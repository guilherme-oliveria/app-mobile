# 4 — Ciclo de Vida Completo de um Pedido

> Do momento que a loja cria o pedido até a liquidação do dinheiro — passo a passo.

---

## Diagrama Completo

> **Modelo Híbrido**: motoboy aceita sozinho OU admin atribui manualmente.
> Detalhes em [13-modelo-atribuicao-entregas.md](./13-modelo-atribuicao-entregas.md)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║                    CICLO DE VIDA DE UM PEDIDO                               ║
╠══════════════════════════════════════════════════════════════════════════════╣
║                                                                              ║
║  ① LOJA cria pedido                                                         ║
║     Store App → POST /api/pedidos                                           ║
║     Status: AGUARDANDO_ACEITE                                               ║
║     ┌──────────────────────────────────┐                                    ║
║     │ Pedido #42                       │                                    ║
║     │ Loja: Pizzaria do João           │                                    ║
║     │ Cliente: Maria Silva             │                                    ║
║     │ Endereço: Rua das Flores, 123    │                                    ║
║     │ Itens: 2x Pizza + 1x Refri      │                                    ║
║     │ Valor: R$ 89,90                  │                                    ║
║     └──────────────────────────────────┘                                    ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ② Backend cria ENTREGA automaticamente                                     ║
║     Status Entrega: DISPONIVEL                                              ║
║     → Push FCM para TODOS os motoboys DISPONÍVEIS                          ║
║     → "Nova entrega disponível! Pizzaria do João"                           ║
║     → Admin também vê no Dashboard em tempo real                            ║
║         │                                                                    ║
║         ├──────────── CAMINHO A (motoboy aceita sozinho) ──────────┐        ║
║         │                                                           │        ║
║         │  ③a MOTOBOY vê lista de entregas disponíveis no app      │        ║
║         │      Motoboy App → GET /api/entregas/disponiveis          │        ║
║         │      Clica "ACEITAR" → POST /api/entregas/{id}/aceitar    │        ║
║         │      Status Entrega: ATRIBUIDA                            │        ║
║         │      Status Pedido:  ACEITO                               │        ║
║         │      Status Motoboy: EM_ENTREGA                           │        ║
║         │                                                           │        ║
║         ├──────────── CAMINHO B (admin atribui) ───────────────────┤        ║
║         │                                                           │        ║
║         │  ③b ADMIN atribui motoboy pelo Dashboard                  │        ║
║         │      Select motoboy → PATCH /api/entregas/{id}/atribuir   │        ║
║         │      Status Entrega: ATRIBUIDA                            │        ║
║         │      Status Pedido:  ACEITO                               │        ║
║         │      Status Motoboy: EM_ENTREGA                           │        ║
║         │      → Push FCM para o motoboy atribuído                  │        ║
║         │                                                           │        ║
║         └───────────────────────┬───────────────────────────────────┘        ║
║                                 │                                            ║
║                                 ▼                                            ║
║  ④ MOTOBOY vê entrega no app (entrega_ativa_screen.dart)                     ║
║     Vê: loja + endereço do cliente + itens + valor                           ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ⑤ MOTOBOY vai até a LOJA e confirma coleta                                 ║
║     Botão "Confirmar coleta" → PATCH /api/entregas/{id}/coletar             ║
║     Status Entrega: COLETADA                                                ║
║     Status Pedido:  COLETADO                                                ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ⑥ MOTOBOY chega no CLIENTE e cobra na maquininha                           ║
║     Botão "Cobrar R$ 89,90" → Diálogo de confirmação                        ║
║     → SDK Smart POS (Stone) processa cartão                                  ║
║     → POST /api/pagamentos/processar                                         ║
║         │                                                                    ║
║         │   ┌──────── SPLIT AUTOMÁTICO (Pagar.me) ───────────┐              ║
║         │   │                                                  │             ║
║         │   │  R$ 89,90 (bruto)                               │             ║
║         │   │    - R$ 2,25 (2,5% Pagar.me)                    │             ║
║         │   │    - R$ 4,50 (5,0% sua taxa)                    │             ║
║         │   │    = R$ 83,15 → conta da Loja (D+1 ou D+2)     │             ║
║         │   │                                                  │             ║
║         │   │  Registra Transação (StatusTransacao.APROVADA)  │             ║
║         │   └──────────────────────────────────────────────────┘             ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ⑦ Entrega finalizada                                                       ║
║     PATCH /api/entregas/{id}/finalizar                                       ║
║     Status Entrega: FINALIZADA                                              ║
║     Status Pedido:  ENTREGUE                                                ║
║     Status Motoboy: DISPONIVEL (liberado para próxima entrega)              ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ⑧ LIQUIDAÇÃO DIÁRIA (23:30 automático via @Scheduled)                      ║
║     PagamentoService.liquidarDiario()                                        ║
║     Agrupa transações APROVADAS não-liquidadas por loja                      ║
║     Gera LiquidacaoDia com resumo do dia                                    ║
║     Admin visualiza em /relatorios (relatorios.component)                    ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## Resumo dos Status em cada etapa

| Etapa | Pedido | Entrega | Motoboy |
|-------|--------|---------|---------|
| ① Loja cria pedido | `AGUARDANDO_ACEITE` | — | — |
| ② Backend cria entrega | `AGUARDANDO_ACEITE` | `DISPONIVEL` | — |
| ③a Motoboy aceita entrega | `ACEITO` | `ATRIBUIDA` | `EM_ENTREGA` |
| ③b Admin atribui motoboy | `ACEITO` | `ATRIBUIDA` | `EM_ENTREGA` |
| ⑤ Motoboy confirma coleta | `COLETADO` | `COLETADA` | `EM_ENTREGA` |
| ⑥ Motoboy cobra na maquininha | `EM_ENTREGA` | `COLETADA` | `EM_ENTREGA` |
| ⑦ Entrega finalizada | `ENTREGUE` | `FINALIZADA` | `DISPONIVEL` |

---

## Código de referência

### Backend — EntregaService.java

```java
// Criar entrega automaticamente quando loja cria pedido
public Entrega criarParaPedido(Long pedidoId) {
    var pedido = pedidoService.buscarEntidade(pedidoId);
    var entrega = entregaRepository.save(Entrega.builder()
            .pedido(pedido).status(DISPONIVEL).build());
    // Notifica todos os motoboys disponíveis via FCM
    // fcmService.notificarEntregaDisponivel(entrega);
    return entrega;
}

// Motoboy aceita entrega (auto-atribuição)
@Transactional
public Entrega aceitarEntrega(Long entregaId) {
    var entrega = buscarEntidade(entregaId);
    if (entrega.getStatus() != DISPONIVEL) {
        throw new BusinessException("Entrega já foi aceita por outro motoboy");
    }
    var motoboyId = SecurityUtils.getRefIdFromToken();
    var motoboy = motoboyService.buscarEntidade(motoboyId);
    if (motoboy.getStatus() != DISPONIVEL) {
        throw new BusinessException("Você já está em uma entrega");
    }
    entrega.setMotoboy(motoboy);
    entrega.setStatus(ATRIBUIDA);
    motoboyService.atualizarStatus(motoboyId, EM_ENTREGA);
    pedidoService.atualizarStatus(entrega.getPedido().getId(), ACEITO);
    return entregaRepository.save(entrega);
}

// Admin atribui motoboy manualmente (intervenção)
public Entrega atribuirMotoboy(Long entregaId, Long motoboyId) {
    var entrega = buscarEntidade(entregaId);
    var motoboy = motoboyService.buscarEntidade(motoboyId);
    entrega.setMotoboy(motoboy);
    entrega.setStatus(ATRIBUIDA);
    motoboyService.atualizarStatus(motoboyId, EM_ENTREGA);
    pedidoService.atualizarStatus(entrega.getPedido().getId(), ACEITO);
    return entregaRepository.save(entrega);
}

// Confirmar coleta
public Entrega confirmarColeta(Long entregaId) {
    var entrega = buscarEntidade(entregaId);
    entrega.setStatus(COLETADA);
    pedidoService.atualizarStatus(entrega.getPedido().getId(), COLETADO);
    return entregaRepository.save(entrega);
}

// Finalizar entrega
public Entrega finalizarEntrega(Long entregaId, String codigo) {
    var entrega = buscarEntidade(entregaId);
    entrega.setStatus(FINALIZADA);
    pedidoService.atualizarStatus(entrega.getPedido().getId(), ENTREGUE);
    motoboyService.atualizarStatus(entrega.getMotoboy().getId(), DISPONIVEL);
    return entregaRepository.save(entrega);
}
```

