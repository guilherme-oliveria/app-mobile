package com.delivery.pagamento.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Integração com Pagar.me v5.
 * Documentação: https://docs.pagar.me/reference
 *
 * ATENÇÃO: métodos com lógica real de chamada HTTP.
 * No ambiente de testes use a chave de sandbox do Pagar.me.
 */
@Slf4j
@Service
public class PagarmeService {

    @Value("${pagarme.api.url}")
    private String apiUrl;

    @Value("${pagarme.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Cria transação com split_rules no Pagar.me.
     *
     * No modelo marketplace:
     *   - recipientId = ID do recebedor da loja (cadastrado no onboarding)
     *   - O restante (taxa da plataforma) fica na sua conta automaticamente
     *
     * @return ID da transação no Pagar.me
     */
    public String criarTransacaoComSplit(String recipientId,
                                          BigDecimal valorBruto,
                                          BigDecimal taxaPlataforma,
                                          String smartPosSerial) {
        // TODO: implementar chamada real ao Pagar.me
        // POST {apiUrl}/orders com split_rules
        // Exemplo de payload:
        // {
        //   "items": [{"amount": valorEmCentavos, "description": "Entrega", "quantity": 1}],
        //   "payments": [{
        //     "payment_method": "credit_card",
        //     "split": [
        //       {"recipient_id": recipientId, "type": "percentage", "amount": 93},
        //       {"recipient_id": "sua_plataforma_id", "type": "percentage", "amount": 7}
        //     ]
        //   }]
        // }

        log.info("Pagar.me [STUB] - Criando transação split para recipient: {} valor: {}",
                recipientId, valorBruto);
        return "pagar_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Cria transferência para o recebedor (liquidação diária).
     * POST /recipients/{recipientId}/withdrawals
     *
     * @return ID da transferência
     */
    public String criarTransferencia(String recipientId, BigDecimal valor) {
        // TODO: implementar chamada real
        // POST {apiUrl}/recipients/{recipientId}/withdrawals
        // { "amount": valorEmCentavos }

        log.info("Pagar.me [STUB] - Transferindo R$ {} para recipient: {}", valor, recipientId);
        return "transfer_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Cadastra a loja como recebedor (onboarding marketplace).
     * Deve ser chamado quando uma loja se cadastra na plataforma.
     * POST /recipients
     */
    public String cadastrarRecebedor(String nome, String cnpj, String chavePix) {
        // TODO: implementar chamada real
        // POST {apiUrl}/recipients
        // { "name": nome, "document": cnpj, "type": "corporation",
        //   "default_bank_account": { "type": "checking", "pix_key": chavePix } }

        log.info("Pagar.me [STUB] - Cadastrando recebedor: {}", nome);
        return "re_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
