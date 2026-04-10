package com.delivery.pedido.dto;

import com.delivery.shared.Enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long lojaId,
        String lojaNome,
        String clienteNome,
        String clienteTelefone,
        String enderecoEntrega,
        List<ItemResponse> itens,
        BigDecimal valorTotal,
        BigDecimal taxaEntrega,
        StatusPedido status,
        String observacao,
        LocalDateTime criadoEm
) {
    public record ItemResponse(
            Long id,
            String descricao,
            Integer quantidade,
            BigDecimal valorUnitario,
            BigDecimal valorTotal
    ) {}
}
