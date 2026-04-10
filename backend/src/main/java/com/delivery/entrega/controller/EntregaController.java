package com.delivery.entrega.controller;

import com.delivery.entrega.entity.Entrega;
import com.delivery.entrega.service.EntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;

    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Entrega>> listarPendentes() {
        return ResponseEntity.ok(entregaService.listarPendentes());
    }

    @GetMapping("/motoboy/{motoboyId}")
    @PreAuthorize("hasAnyRole('ADMIN','MOTOBOY')")
    public ResponseEntity<List<Entrega>> listarPorMotoboy(@PathVariable Long motoboyId) {
        return ResponseEntity.ok(entregaService.listarPorMotoboy(motoboyId));
    }

    @PostMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('ADMIN','LOJA')")
    public ResponseEntity<Entrega> criar(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(entregaService.criarParaPedido(pedidoId));
    }

    @PatchMapping("/{id}/atribuir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Entrega> atribuir(@PathVariable Long id,
                                             @RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(entregaService.atribuirMotoboy(id, body.get("motoboyId")));
    }

    @PatchMapping("/{id}/coletar")
    @PreAuthorize("hasRole('MOTOBOY')")
    public ResponseEntity<Entrega> confirmarColeta(@PathVariable Long id) {
        return ResponseEntity.ok(entregaService.confirmarColeta(id));
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('MOTOBOY')")
    public ResponseEntity<Entrega> finalizar(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(entregaService.finalizarEntrega(id, body.get("codigoConfirmacao")));
    }
}
