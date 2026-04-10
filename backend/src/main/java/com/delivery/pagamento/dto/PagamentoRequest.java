package com.delivery.pagamento.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PagamentoRequest(
        @NotNull Long entregaId,
        @NotNull BigDecimal valor,
        @NotNull String smartPosSerial,
        String formaPagamento  // "credit_card" | "debit_card" | "pix"
) {}
