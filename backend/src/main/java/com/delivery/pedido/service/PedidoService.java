package com.delivery.pedido.service;

import com.delivery.loja.service.LojaService;
import com.delivery.pedido.dto.PedidoRequest;
import com.delivery.pedido.dto.PedidoResponse;
import com.delivery.pedido.entity.ItemPedido;
import com.delivery.pedido.entity.Pedido;
import com.delivery.pedido.repository.PedidoRepository;
import com.delivery.shared.Enums.StatusPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final LojaService lojaService;

    public List<PedidoResponse> listarPorLoja(Long lojaId) {
        return pedidoRepository.findByLojaIdOrderByCriadoEmDesc(lojaId)
                .stream().map(this::toResponse).toList();
    }

    public List<PedidoResponse> listarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status)
                .stream().map(this::toResponse).toList();
    }

    public PedidoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        var loja = lojaService.buscarEntidade(request.lojaId());

        var pedido = Pedido.builder()
                .loja(loja)
                .clienteNome(request.clienteNome())
                .clienteTelefone(request.clienteTelefone())
                .enderecoEntrega(request.enderecoEntrega())
                .latitudeEntrega(request.latitudeEntrega())
                .longitudeEntrega(request.longitudeEntrega())
                .taxaEntrega(request.taxaEntrega() != null ? request.taxaEntrega() : BigDecimal.ZERO)
                .observacao(request.observacao())
                .build();

        // mapear itens
        var itens = request.itens().stream().map(i -> {
            var total = i.valorUnitario().multiply(BigDecimal.valueOf(i.quantidade()));
            return ItemPedido.builder()
                    .pedido(pedido)
                    .descricao(i.descricao())
                    .quantidade(i.quantidade())
                    .valorUnitario(i.valorUnitario())
                    .valorTotal(total)
                    .build();
        }).toList();

        pedido.setItens(itens);

        BigDecimal totalItens = itens.stream()
                .map(ItemPedido::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setValorTotal(totalItens.add(pedido.getTaxaEntrega()));

        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse atualizarStatus(Long id, StatusPedido status) {
        var pedido = buscarEntidade(id);
        pedido.setStatus(status);
        return toResponse(pedidoRepository.save(pedido));
    }

    public Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
    }

    private PedidoResponse toResponse(Pedido p) {
        var itensResp = p.getItens() == null ? List.<PedidoResponse.ItemResponse>of() :
                p.getItens().stream().map(i -> new PedidoResponse.ItemResponse(
                        i.getId(), i.getDescricao(), i.getQuantidade(),
                        i.getValorUnitario(), i.getValorTotal())).toList();

        return new PedidoResponse(
                p.getId(), p.getLoja().getId(), p.getLoja().getNome(),
                p.getClienteNome(), p.getClienteTelefone(), p.getEnderecoEntrega(),
                itensResp, p.getValorTotal(), p.getTaxaEntrega(),
                p.getStatus(), p.getObservacao(), p.getCriadoEm()
        );
    }
}
