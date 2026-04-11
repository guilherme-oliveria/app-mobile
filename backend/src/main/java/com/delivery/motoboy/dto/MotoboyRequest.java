package com.delivery.motoboy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotoboyRequest(
        @NotBlank String nome,
        @NotBlank String cpf,
        @NotBlank String email,
        String telefone,
        String cnh,
        String smartPosSerial,
        @NotBlank @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String senhaInicial
) {}
