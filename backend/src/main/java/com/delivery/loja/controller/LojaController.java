package com.delivery.loja.controller;

import com.delivery.loja.dto.LojaRequest;
import com.delivery.loja.dto.LojaResponse;
import com.delivery.loja.service.LojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lojas")
@RequiredArgsConstructor
public class LojaController {

    private final LojaService lojaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE')")
    public ResponseEntity<List<LojaResponse>> listar() {
        return ResponseEntity.ok(lojaService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','LOJA')")
    public ResponseEntity<LojaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(lojaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE')")
    public ResponseEntity<LojaResponse> criar(@RequestBody @Valid LojaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lojaService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','LOJA')")
    public ResponseEntity<LojaResponse> atualizar(@PathVariable Long id,
                                                   @RequestBody @Valid LojaRequest request) {
        return ResponseEntity.ok(lojaService.atualizar(id, request));
    }

    // ❌ SUPORTE não pode inativar — somente ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        lojaService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
