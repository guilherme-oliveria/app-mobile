package com.delivery.motoboy.controller;

import com.delivery.motoboy.dto.*;
import com.delivery.motoboy.service.MotoboyService;
import com.delivery.shared.Enums.StatusMotoboy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/motoboys")
@RequiredArgsConstructor
public class MotoboyController {

    private final MotoboyService motoboyService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MotoboyResponse>> listar() {
        return ResponseEntity.ok(motoboyService.listarTodos());
    }

    @GetMapping("/disponiveis")
    @PreAuthorize("hasAnyRole('ADMIN','LOJA')")
    public ResponseEntity<List<MotoboyResponse>> listarDisponiveis() {
        return ResponseEntity.ok(motoboyService.listarDisponiveis());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MOTOBOY')")
    public ResponseEntity<MotoboyResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(motoboyService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MotoboyResponse> criar(@RequestBody @Valid MotoboyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(motoboyService.criar(request));
    }

    @PatchMapping("/{id}/localizacao")
    @PreAuthorize("hasRole('MOTOBOY')")
    public ResponseEntity<Void> atualizarLocalizacao(@PathVariable Long id,
                                                      @RequestBody LocalizacaoRequest request) {
        motoboyService.atualizarLocalizacao(id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MOTOBOY')")
    public ResponseEntity<Void> atualizarStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body) {
        motoboyService.atualizarStatus(id, StatusMotoboy.valueOf(body.get("status")));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/fcm-token")
    @PreAuthorize("hasRole('MOTOBOY')")
    public ResponseEntity<Void> atualizarFcmToken(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        motoboyService.atualizarFcmToken(id, body.get("token"));
        return ResponseEntity.ok().build();
    }
}
