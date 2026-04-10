package com.delivery.pagamento.service;

import com.delivery.entrega.service.EntregaService;
import com.delivery.loja.repository.LojaRepository;
import com.delivery.pagamento.dto.PagamentoRequest;
import com.delivery.pagamento.entity.LiquidacaoDia;
import com.delivery.pagamento.entity.Transacao;
import com.delivery.pagamento.repository.LiquidacaoRepository;
import com.delivery.pagamento.repository.TransacaoRepository;
import com.delivery.shared.Enums.StatusLiquidacao;
import com.delivery.shared.Enums.StatusTransacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoService {

    // Taxa da plataforma: 5%
    private static final BigDecimal TAXA_PLATAFORMA = new BigDecimal("0.05");
    // Taxa do gateway (Pagar.me): 2.5%
    private static final BigDecimal TAXA_GATEWAY = new BigDecimal("0.025");

    private final TransacaoRepository transacaoRepository;
    private final LiquidacaoRepository liquidacaoRepository;
    private final EntregaService entregaService;
    private final LojaRepository lojaRepository;
    private final PagarmeService pagarmeService; // integração Pagar.me

    /**
     * Chamado pelo app do motoboy ao apresentar a maquininha ao cliente.
     * Cria a transação no Pagar.me com split automático.
     */
    @Transactional
    public Transacao processarPagamento(PagamentoRequest request) {
        var entrega = entregaService.buscarEntidade(request.entregaId());
        var valorBruto = request.valor();

        // calcular split
        var taxaGateway = valorBruto.multiply(TAXA_GATEWAY).setScale(2, RoundingMode.HALF_UP);
        var taxaPlataforma = valorBruto.multiply(TAXA_PLATAFORMA).setScale(2, RoundingMode.HALF_UP);
        var valorLiquidoLoja = valorBruto.subtract(taxaGateway).subtract(taxaPlataforma);

        // chama Pagar.me para criar a cobrança com split_rules
        String pagarmeId = pagarmeService.criarTransacaoComSplit(
                entrega.getPedido().getLoja().getPagarmeRecipientId(),
                valorBruto,
                taxaPlataforma,
                request.smartPosSerial()
        );

        var transacao = Transacao.builder()
                .entrega(entrega)
                .valorBruto(valorBruto)
                .taxaGateway(taxaGateway)
                .taxaPlataforma(taxaPlataforma)
                .valorLiquidoLoja(valorLiquidoLoja)
                .pagarmeTransactionId(pagarmeId)
                .smartPosSerial(request.smartPosSerial())
                .status(StatusTransacao.APROVADA)
                .processadaEm(LocalDateTime.now())
                .build();

        return transacaoRepository.save(transacao);
    }

    /**
     * Agendado todo dia às 23:30 para liquidar os saldos das lojas.
     * Cron: segundo minuto hora dia mês diaDaSemana
     */
    @Scheduled(cron = "0 30 23 * * *")
    @Transactional
    public void liquidarDiario() {
        log.info("Iniciando liquidação diária - {}", LocalDate.now());

        var transacoesPendentes = transacaoRepository
                .findByLiquidadaFalseAndStatus(StatusTransacao.APROVADA);

        // agrupar por loja
        var porLoja = transacoesPendentes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getEntrega().getPedido().getLoja()
                ));

        porLoja.forEach((loja, transacoes) -> {
            var totalBruto = transacoes.stream()
                    .map(Transacao::getValorBruto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var totalTaxas = transacoes.stream()
                    .map(t -> t.getTaxaGateway().add(t.getTaxaPlataforma()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var valorLiquido = totalBruto.subtract(totalTaxas);

            // dispara transferência no Pagar.me para o recebedor da loja
            String transferId = pagarmeService.criarTransferencia(
                    loja.getPagarmeRecipientId(), valorLiquido);

            // registra liquidação
            var liquidacao = LiquidacaoDia.builder()
                    .loja(loja)
                    .dataReferencia(LocalDate.now())
                    .totalEntregas(transacoes.size())
                    .valorBruto(totalBruto)
                    .totalTaxas(totalTaxas)
                    .valorLiquido(valorLiquido)
                    .pagarmeTransferId(transferId)
                    .status(StatusLiquidacao.PROCESSADA)
                    .processadaEm(LocalDateTime.now())
                    .build();

            liquidacaoRepository.save(liquidacao);

            // marca transações como liquidadas
            transacoes.forEach(t -> t.setLiquidada(true));
            transacaoRepository.saveAll(transacoes);

            log.info("Loja {} liquidada: R$ {}", loja.getNome(), valorLiquido);
        });

        log.info("Liquidação diária concluída");
    }

    public List<LiquidacaoDia> listarLiquidacoesPorLoja(Long lojaId) {
        return liquidacaoRepository.findByLojaIdOrderByDataReferenciaDesc(lojaId);
    }
}
