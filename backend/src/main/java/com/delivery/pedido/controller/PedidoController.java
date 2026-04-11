package com.delivery.pedido.controller;

import com.delivery.pedido.dto.PedidoRequest;
import com.delivery.pedido.dto.PedidoResponse;
import com.delivery.pedido.service.PedidoService;
import com.delivery.shared.Enums.StatusPedido;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping("/loja/{lojaId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','LOJA')")
    public ResponseEntity<List<PedidoResponse>> listarPorLoja(@PathVariable Long lojaId) {
        return ResponseEntity.ok(pedidoService.listarPorLoja(lojaId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','MOTOBOY')")
    public ResponseEntity<List<PedidoResponse>> listarPorStatus(@PathVariable StatusPedido status) {
        return ResponseEntity.ok(pedidoService.listarPorStatus(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPORTE','LOJA','MOTOBOY')")
    public ResponseEntity<PedidoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOJA')")
    public ResponseEntity<PedidoResponse> criar(@RequestBody @Valid PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(request));
    }

    // ❌ SUPORTE não pode alterar status (inclui CANCELADO) — somente ADMIN, LOJA ou MOTOBOY
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','LOJA','MOTOBOY')")
    public ResponseEntity<PedidoResponse> atualizarStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                pedidoService.atualizarStatus(id, StatusPedido.valueOf(body.get("status"))));
    }
}
