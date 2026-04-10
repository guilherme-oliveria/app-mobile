package com.delivery.loja.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LojaRequest(
        @NotBlank String nome,
        @NotBlank String cnpj,
        @NotBlank @Email String email,
        String telefone,
        String endereco,
        String chavePix
) {}
