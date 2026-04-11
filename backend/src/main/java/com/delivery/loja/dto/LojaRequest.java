package com.delivery.loja.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LojaRequest(
        @NotBlank String nome,
        @NotBlank String cnpj,
        @NotBlank @Email String email,
        String telefone,
        String endereco,
        String chavePix,
        @NotBlank @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String senhaInicial
) {}
