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

    /**
     * Lista entregas disponíveis (status DISPONIVEL) — para motoboy e admin.
     * Motoboy: vê no app e pode aceitar.
     * Admin: vê no dashboard e pode atribuir manualmente.
     */
    @GetMapping("/disponiveis")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','MOTOBOY')")
    public ResponseEntity<List<Entrega>> listarDisponiveis() {
        return ResponseEntity.ok(entregaService.listarDisponiveis());
    }

    @GetMapping("/motoboy/{motoboyId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','MOTOBOY')")
    public ResponseEntity<List<Entrega>> listarPorMotoboy(@PathVariable Long motoboyId) {
        return ResponseEntity.ok(entregaService.listarPorMotoboy(motoboyId));
    }

    @PostMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','LOJA')")
    public ResponseEntity<Entrega> criar(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(entregaService.criarParaPedido(pedidoId));
    }

    @PostMapping("/{id}/aceitar")
    @PreAuthorize("hasRole('MOTOBOY')")
    public ResponseEntity<Entrega> aceitar(@PathVariable Long id) {
        return ResponseEntity.ok(entregaService.aceitarEntrega(id));
    }

    // SUPORTE pode atribuir motoboy manualmente
    @PatchMapping("/{id}/atribuir")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE')")
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
