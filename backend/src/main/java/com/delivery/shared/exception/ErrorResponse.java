package com.delivery.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta de erro padronizada para o frontend Angular.
 * Formato consistente: { status, erro, mensagem, campos[], timestamp }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String erro,
        String mensagem,
        List<CampoErro> campos,
        String timestamp
) {

    public record CampoErro(String campo, String mensagem) {}

    public static ErrorResponse of(int status, String erro, String mensagem) {
        return new ErrorResponse(status, erro, mensagem, null, LocalDateTime.now().toString());
    }

    public static ErrorResponse of(int status, String erro, String mensagem, List<CampoErro> campos) {
        return new ErrorResponse(status, erro, mensagem, campos, LocalDateTime.now().toString());
    }
}

