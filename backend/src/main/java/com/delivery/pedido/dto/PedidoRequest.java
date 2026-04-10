package com.delivery.pedido.dto;

import com.delivery.shared.Enums.StatusPedido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoRequest(
        @NotNull Long lojaId,
        @NotBlank String clienteNome,
        String clienteTelefone,
        @NotBlank String enderecoEntrega,
        Double latitudeEntrega,
        Double longitudeEntrega,
        @NotNull List<ItemRequest> itens,
        BigDecimal taxaEntrega,
        String observacao
) {
    public record ItemRequest(
            @NotBlank String descricao,
            @NotNull Integer quantidade,
            @NotNull BigDecimal valorUnitario
    ) {}
}
