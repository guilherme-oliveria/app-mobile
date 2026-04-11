package com.delivery.pagamento.controller;

import com.delivery.pagamento.dto.PagamentoRequest;
import com.delivery.pagamento.entity.LiquidacaoDia;
import com.delivery.pagamento.entity.Transacao;
import com.delivery.pagamento.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    /**
     * Endpoint chamado pelo app do motoboy após o cliente pagar na maquininha.
     */
    @PostMapping("/processar")
    @PreAuthorize("hasRole('MOTOBOY')")
    public ResponseEntity<Transacao> processar(@RequestBody @Valid PagamentoRequest request) {
        return ResponseEntity.ok(pagamentoService.processarPagamento(request));
    }

    /**
     * Histórico de liquidações de uma loja.
     * ❌ SUPORTE não tem acesso — dados financeiros sensíveis.
     */
    @GetMapping("/liquidacoes/loja/{lojaId}")
    @PreAuthorize("hasAnyRole('ADMIN','LOJA')")
    public ResponseEntity<List<LiquidacaoDia>> liquidacoesPorLoja(@PathVariable Long lojaId) {
        return ResponseEntity.ok(pagamentoService.listarLiquidacoesPorLoja(lojaId));
    }

    /**
     * Dispara liquidação manualmente.
     * ❌ SUPORTE não tem acesso.
     */
    @PostMapping("/liquidar-agora")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> liquidarAgora() {
        pagamentoService.liquidarDiario();
        return ResponseEntity.ok().build();
    }
}
