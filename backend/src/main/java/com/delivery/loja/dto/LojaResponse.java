package com.delivery.loja.dto;

import java.math.BigDecimal;

public record LojaResponse(
        Long id,
        String nome,
        String cnpj,
        String email,
        String telefone,
        String endereco,
        boolean ativo,
        BigDecimal saldoPendente
) {}
