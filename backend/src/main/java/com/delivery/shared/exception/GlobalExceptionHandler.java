package com.delivery.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ── Validação @Valid (campos inválidos) ─────────────────── */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorResponse.CampoErro(f.getField(), f.getDefaultMessage()))
                .toList();

        log.warn("Validação falhou: {}", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Validação", "Um ou mais campos são inválidos", campos));
    }

    /* ── Regra de negócio ────────────────────────────────────── */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("Erro de negócio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(422, "Regra de negócio", ex.getMessage()));
    }

    /* ── Recurso não encontrado ──────────────────────────────── */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Não encontrado", ex.getMessage()));
    }

    /* ── Constraint de banco (unique, not-null etc.) ─────────── */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());

        String mensagem = "Erro de integridade de dados";
        String detalhe = ex.getMostSpecificCause().getMessage();

        if (detalhe != null) {
            if (detalhe.contains("unique") || detalhe.contains("duplicate") || detalhe.contains("duplicar")) {
                mensagem = "Registro duplicado — já existe um cadastro com esses dados";
            } else if (detalhe.contains("not-null") || detalhe.contains("null value")) {
                // Extrai o nome da coluna do erro do PostgreSQL
                // Formato: "null value in column \"criado_em\" of relation \"usuarios\""
                String coluna = extrairColuna(detalhe);
                mensagem = coluna != null
                        ? "Campo obrigatório não preenchido: " + coluna
                        : "Um campo obrigatório não foi preenchido";
            } else if (detalhe.contains("foreign key") || detalhe.contains("referential")) {
                mensagem = "Referência inválida — registro relacionado não encontrado";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Integridade de dados", mensagem));
    }

    /** Extrai o nome da coluna de mensagens de erro PostgreSQL */
    private String extrairColuna(String detalhe) {
        // Formato PostgreSQL: null value in column "nome_coluna" of relation "tabela"
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("column \"(\\w+)\"").matcher(detalhe);
        return m.find() ? m.group(1) : null;
    }

    /* ── Optimistic Lock (concorrência) ──────────────────────── */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflito de concorrência: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflito", "Esta entrega já foi aceita por outro motoboy. Tente outra."));
    }

    /* ── Acesso negado (role insuficiente) ────────────────────── */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "Acesso negado", "Você não tem permissão para esta ação"));
    }

    /* ── Erro genérico (catch-all) ────────────────────────────── */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Erro interno", "Erro interno do servidor"));
    }
}
