package com.delivery.motoboy.dto;

import jakarta.validation.constraints.NotBlank;

public record MotoboyRequest(
        @NotBlank String nome,
        @NotBlank String cpf,
        @NotBlank String email,
        String telefone,
        String cnh,
        String smartPosSerial
) {}
