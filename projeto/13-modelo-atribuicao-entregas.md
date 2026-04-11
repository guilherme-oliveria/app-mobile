# 13 — Modelo de Atribuição de Entregas: Admin vs Auto-Atribuição

> Análise completa de quem atribui o motoboy à entrega e recomendação final.

---

## Os 3 Modelos Possíveis

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                 MODELO A: ADMIN ATRIBUI (como está hoje)                    │
│                                                                             │
│  Loja cria pedido → Admin vê → Admin escolhe motoboy → Motoboy recebe     │
│                                                                             │
│  ✅ Controle total do admin                                                 │
│  ✅ Pode escolher motoboy mais próximo/com menos entregas                   │
│  ❌ GARGALO: admin vira despachante 24h                                     │
│  ❌ Não escala: 10 lojas = OK, 100 lojas = impossível                       │
│  ❌ Delay: motoboy fica parado esperando admin atribuir                     │
│  ❌ Ponto único de falha: admin dormiu = ninguém entrega                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                 MODELO B: MOTOBOY SE AUTO-ATRIBUI                           │
│                                                                             │
│  Loja cria pedido → Motoboy vê lista → Motoboy aceita → Vai buscar        │
│                                                                             │
│  ✅ Escala infinitamente                                                    │
│  ✅ Admin não precisa ficar online 24h                                      │
│  ✅ Mais rápido (motoboy aceita na hora)                                    │
│  ❌ "Cherry-picking": motoboy só pega entrega boa (perto, valor alto)       │
│  ❌ Entregas ruins ficam abandonadas                                        │
│  ❌ Dois motoboys tentam aceitar ao mesmo tempo (race condition)            │
│  ❌ Sem controle de quem entrega para onde                                  │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│           MODELO C: HÍBRIDO (✅ RECOMENDADO para seu projeto)               │
│                                                                             │
│  Loja cria pedido                                                           │
│    → Notifica motoboys disponíveis (push FCM)                               │
│    → Motoboy vê lista de entregas disponíveis no app                        │
│    → Motoboy clica "ACEITAR" → se auto-atribui                             │
│    → Admin pode INTERVIR: reatribuir, cancelar, forçar atribuição          │
│                                                                             │
│  ✅ Escala (motoboy age sozinho no dia-a-dia)                               │
│  ✅ Admin tem poder total para intervir quando necessário                   │
│  ✅ Sem gargalo (admin não precisa atribuir cada entrega)                   │
│  ✅ Rápido (motoboy aceita imediatamente via push)                          │
│  ✅ Simples de implementar (poucas mudanças no que já tem)                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Comparativo Direto

| Critério                     | A (Admin)  | B (Auto)   | C (Híbrido) |
|------------------------------|:----------:|:----------:|:-----------:|
| Escala com muitas lojas      | ❌ Péssimo | ✅ Ótimo   | ✅ Ótimo    |
| Controle do admin            | ✅ Total   | ❌ Nenhum  | ✅ Quando quiser |
| Velocidade de atribuição     | ❌ Lento   | ✅ Rápido  | ✅ Rápido   |
| Admin precisa estar online   | ❌ Sempre  | ✅ Não     | ✅ Só quando quer |
| Evita cherry-picking         | ✅ Sim     | ❌ Não     | ⚠️ Parcial  |
| Race condition               | ✅ Não tem | ❌ Pode ter | ⚠️ Trata com lock |
| Complexidade de implementar  | ✅ Simples | ✅ Simples | ⚠️ Moderada |

---

## ✅ RECOMENDAÇÃO: Modelo Híbrido (C)

### Como funciona na prática

```
╔══════════════════════════════════════════════════════════════════════════════╗
║            FLUXO HÍBRIDO — Motoboy aceita + Admin intervém                  ║
╠══════════════════════════════════════════════════════════════════════════════╣
║                                                                              ║
║  ① LOJA cria pedido                                                         ║
║     Status Pedido: AGUARDANDO_ACEITE                                        ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ② Backend cria entrega automaticamente                                     ║
║     Status Entrega: DISPONIVEL (novo status!)                               ║
║     → Push FCM para TODOS os motoboys DISPONÍVEIS da região                ║
║     → "Nova entrega disponível! Pizzaria do João → Rua das Flores"         ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ③ MOTOBOY vê no app (lista de entregas disponíveis)                        ║
║     ┌─────────────────────────────────┐                                     ║
║     │ 🔔 Entregas Disponíveis         │                                     ║
║     │                                 │                                     ║
║     │ #42 - Pizzaria do João          │                                     ║
║     │ 📍 Rua das Flores, 123          │                                     ║
║     │ 💰 R$ 89,90                     │                                     ║
║     │ [    ACEITAR ENTREGA    ]       │                                     ║
║     │                                 │                                     ║
║     │ #43 - Burger King              │                                     ║
║     │ 📍 Av. Brasil, 456             │                                     ║
║     │ 💰 R$ 45,00                     │                                     ║
║     │ [    ACEITAR ENTREGA    ]       │                                     ║
║     └─────────────────────────────────┘                                     ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ④ MOTOBOY clica "ACEITAR ENTREGA"                                          ║
║     POST /api/entregas/{id}/aceitar   (novo endpoint!)                      ║
║     → Backend verifica se entrega ainda está DISPONIVEL                     ║
║     → Se sim: atribui, Status Entrega → ATRIBUIDA                          ║
║     → Se não: "Entrega já foi aceita por outro motoboy"                    ║
║     → Push FCM para OUTROS motoboys: remove entrega da lista              ║
║         │                                                                    ║
║         ▼                                                                    ║
║  ⑤ Segue fluxo normal (coleta → cobrança → finalizar)                      ║
║                                                                              ║
║  ──────────────────────────────────────────────────────────────────          ║
║                                                                              ║
║  🛡️ ADMIN PODE INTERVIR A QUALQUER MOMENTO:                                ║
║                                                                              ║
║  • Ver entregas DISPONÍVEIS no dashboard (ninguém aceitou ainda)            ║
║  • Atribuir manualmente: PATCH /api/entregas/{id}/atribuir                  ║
║  • Reatribuir: trocar motoboy de uma entrega ATRIBUIDA                      ║
║  • Cancelar entrega                                                          ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## Mudanças necessárias no projeto

### 1. Novo status de Entrega: `DISPONIVEL`

```java
public enum StatusEntrega {
    DISPONIVEL,    // ← NOVO (antes era PENDENTE)
    ATRIBUIDA,
    COLETADA,
    FINALIZADA,
    CANCELADA
}
```

> `PENDENTE` → `DISPONIVEL` = a entrega está visível para todos os motoboys aceitarem.

### 2. Novo endpoint: Motoboy aceita entrega

```java
// EntregaController.java
@PatchMapping("/{id}/aceitar")
@PreAuthorize("hasRole('MOTOBOY')")
public ResponseEntity<EntregaDTO> aceitarEntrega(@PathVariable Long id) {
    return ResponseEntity.ok(entregaService.aceitarEntrega(id));
}
```

### 3. Lógica com lock otimista (evitar race condition)

```java
// EntregaService.java
@Transactional
public EntregaDTO aceitarEntrega(Long entregaId) {
    var entrega = entregaRepository.findById(entregaId)
        .orElseThrow(() -> new NotFoundException("Entrega não encontrada"));

    if (entrega.getStatus() != StatusEntrega.DISPONIVEL) {
        throw new BusinessException("Entrega já foi aceita por outro motoboy");
    }

    // Pega o motoboy logado via SecurityContext
    var motoboyId = SecurityUtils.getRefIdFromToken();
    var motoboy = motoboyService.buscarEntidade(motoboyId);

    if (motoboy.getStatus() != StatusMotoboy.DISPONIVEL) {
        throw new BusinessException("Você já está em uma entrega");
    }

    entrega.setMotoboy(motoboy);
    entrega.setStatus(StatusEntrega.ATRIBUIDA);
    motoboyService.atualizarStatus(motoboyId, StatusMotoboy.EM_ENTREGA);
    pedidoService.atualizarStatus(entrega.getPedido().getId(), StatusPedido.ACEITO);

    // Notifica outros motoboys que a entrega não está mais disponível
    // fcmService.notificarEntregaAceita(entregaId);

    return EntregaDTO.from(entregaRepository.save(entrega));
}
```

### 4. Novo endpoint: Listar entregas disponíveis (para o app do motoboy)

```java
// EntregaController.java
@GetMapping("/disponiveis")
@PreAuthorize("hasRole('MOTOBOY')")
public ResponseEntity<List<EntregaDTO>> listarDisponiveis() {
    return ResponseEntity.ok(entregaService.listarDisponiveis());
}
```

### 5. Motoboy App (Flutter) — Nova tela de entregas disponíveis

```dart
// home_screen.dart — ao invés de só ver "entrega atribuída", motoboy vê lista
// GET /api/entregas/disponiveis → lista de cards
// Botão "ACEITAR" → POST /api/entregas/{id}/aceitar
```

### 6. Dashboard Admin — Visão e intervenção

O dashboard continua mostrando entregas pendentes, mas agora:
- Mostra entregas `DISPONIVEL` que ninguém aceitou ainda
- Admin pode clicar e atribuir manualmente (como faz hoje)
- Admin vê quem aceitou cada entrega

---

## Timeout: Entrega não aceita em X minutos

Para evitar que uma entrega fique parada sem ninguém aceitar:

```java
// @Scheduled job — roda a cada 2 minutos
@Scheduled(fixedRate = 120_000)
public void verificarEntregasSemAceite() {
    var limite = LocalDateTime.now().minusMinutes(10);
    var entregas = entregaRepository
        .findByStatusAndCriadoEmBefore(StatusEntrega.DISPONIVEL, limite);

    for (var entrega : entregas) {
        // Notifica admin: "Entrega #42 sem motoboy há 10 min"
        // fcmService.alertarAdmin(entrega);
        // Ou envia novo push para motoboys com valor de urgência
    }
}
```

---

## Fluxo atualizado de status

| Etapa | Pedido | Entrega | Motoboy |
|-------|--------|---------|---------|
| ① Loja cria pedido | `AGUARDANDO_ACEITE` | — | — |
| ② Backend cria entrega auto | `AGUARDANDO_ACEITE` | `DISPONIVEL` | — |
| ③ Motoboy aceita entrega | `ACEITO` | `ATRIBUIDA` | `EM_ENTREGA` |
| ③' Admin atribui (alternativa) | `ACEITO` | `ATRIBUIDA` | `EM_ENTREGA` |
| ④ Motoboy confirma coleta | `COLETADO` | `COLETADA` | `EM_ENTREGA` |
| ⑤ Motoboy cobra na maquininha | `EM_ENTREGA` | `COLETADA` | `EM_ENTREGA` |
| ⑥ Entrega finalizada | `ENTREGUE` | `FINALIZADA` | `DISPONIVEL` |

---

## Diagrama de estado da Entrega (atualizado)

```
                                  ADMIN atribui
DISPONIVEL ────────────────────────────────────────► ATRIBUIDA
     │                                                   │
     │  MOTOBOY aceita                                   │
     └──────────────────────────────────────────────────►│
                                                         │
                                                         ▼
                                                      COLETADA
                                                         │
                                                         ▼
                                                     FINALIZADA

     Qualquer status ──────────────────────────────► CANCELADA
```

---

## Apps do mercado que usam cada modelo

| App | Modelo | Observação |
|-----|--------|-----------|
| **iFood** | Híbrido (automático por IA) | IA atribui, motoboy aceita/recusa |
| **Uber Eats** | Auto-atribuição | Motoboy aceita corrida |
| **Rappi** | Auto-atribuição | Motoboy vê e aceita |
| **99Food** | Híbrido | Despacho + aceite |
| **Loggi** | Admin despacha | B2B, precisa controle |

> Para seu caso (plataforma pequena/média, com admin ativo), o **híbrido** é perfeito:
> - No início com poucos motoboys: admin atribui manualmente
> - Quando crescer: motoboy aceita sozinho, admin só intervém

---

## Resumo

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│   INÍCIO DO NEGÓCIO (poucas lojas):                     │
│   → Admin atribui manualmente (já funciona hoje)        │
│                                                          │
│   CRESCIMENTO (muitas lojas/motoboys):                  │
│   → Motoboy aceita sozinho via app                      │
│   → Admin só intervém quando necessário                 │
│                                                          │
│   ESCALA TOTAL (futuro):                                │
│   → Algoritmo atribui automaticamente (por distância)   │
│   → Motoboy só aceita/recusa                            │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

> **O modelo híbrido permite que o sistema evolua naturalmente sem reescrever nada.**

