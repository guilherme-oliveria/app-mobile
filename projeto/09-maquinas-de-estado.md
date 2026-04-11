# 9 — Máquinas de Estado (Status)

> Todos os status possíveis de Pedido, Entrega e Motoboy, com as transições válidas.

---

## Status do Pedido

```
                                      CANCELADO
                                         ▲
                                         │ (a qualquer momento)
                                         │
AGUARDANDO_ACEITE ──► ACEITO ──► MOTOBOY_A_CAMINHO_LOJA ──► COLETADO ──► EM_ENTREGA ──► ENTREGUE
```

| Status | Significado | Quem muda | Como muda |
|--------|------------|-----------|-----------|
| `AGUARDANDO_ACEITE` | Loja criou o pedido, esperando admin aceitar | — | Criação do pedido |
| `ACEITO` | Admin aceitou e atribuiu motoboy | Admin | Ao atribuir motoboy à entrega |
| `MOTOBOY_A_CAMINHO_LOJA` | Motoboy está indo buscar na loja | Backend | Após atribuição |
| `COLETADO` | Motoboy pegou na loja | Motoboy | Confirmar coleta no app |
| `EM_ENTREGA` | Motoboy está indo até o cliente | Backend | Após coleta |
| `ENTREGUE` | Cliente recebeu e pagou | Motoboy | Finalizar entrega no app |
| `CANCELADO` | Cancelado (qualquer motivo) | Admin/Loja | Cancelamento |

### Enum no backend (Enums.java)
```java
public enum StatusPedido {
    AGUARDANDO_ACEITE,
    ACEITO,
    MOTOBOY_A_CAMINHO_LOJA,
    COLETADO,
    EM_ENTREGA,
    ENTREGUE,
    CANCELADO
}
```

---

## Status da Entrega

> **Modelo Híbrido**: motoboy pode aceitar sozinho OU admin pode atribuir manualmente.
> Veja detalhes completos em [13-modelo-atribuicao-entregas.md](./13-modelo-atribuicao-entregas.md)

```
                    ┌─── Motoboy aceita (POST /entregas/{id}/aceitar)
                    │
DISPONIVEL ─────────┤
                    │
                    └─── Admin atribui (PATCH /entregas/{id}/atribuir)
                              │
                              ▼
                          ATRIBUIDA ──────► COLETADA ──────► FINALIZADA
                              │                │                  │
                              └────────────────┴──────────────────┴──► CANCELADA
```

| Status | Significado | Quem muda | Endpoint |
|--------|------------|-----------|----------|
| `DISPONIVEL` | Entrega criada, visível para todos os motoboys | Backend (auto) | `POST /entregas/pedido/{id}` |
| `ATRIBUIDA` | Motoboy aceito/atribuído, indo buscar | Motoboy ou Admin | `POST /entregas/{id}/aceitar` ou `PATCH /entregas/{id}/atribuir` |
| `COLETADA` | Motoboy pegou na loja | Motoboy | `PATCH /entregas/{id}/coletar` |
| `FINALIZADA` | Entregue + pago | Motoboy | `PATCH /entregas/{id}/finalizar` |
| `CANCELADA` | Cancelada | Admin | Cancelamento |

### Enum no backend
```java
public enum StatusEntrega {
    DISPONIVEL,
    ATRIBUIDA,
    COLETADA,
    FINALIZADA,
    CANCELADA
}
```

---

## Status do Motoboy

```
DISPONIVEL ◄──────────► EM_ENTREGA
     │
     │ (admin desativa)
     ▼
  INATIVO
```

| Status | Significado | Quando muda |
|--------|------------|-------------|
| `DISPONIVEL` | Livre para receber entregas | Após finalizar entrega / início do turno |
| `EM_ENTREGA` | Ocupado com uma entrega | Quando entrega é atribuída a ele |
| `INATIVO` | Desativado pelo admin | Admin desativa manualmente |

### Transições automáticas
- `DISPONIVEL → EM_ENTREGA`: quando motoboy aceita (`aceitarEntrega()`) ou admin atribui (`atribuirMotoboy()`)
- `EM_ENTREGA → DISPONIVEL`: quando motoboy finaliza entrega → `EntregaService.finalizarEntrega()`

### Enum no backend
```java
public enum StatusMotoboy {
    DISPONIVEL,
    EM_ENTREGA,
    INATIVO
}
```

---

## Status da Transação (Pagamento)

```
PENDENTE ──────► APROVADA ──────► (liquidada=true)
    │
    ├──────► RECUSADA
    │
    └──────► ESTORNADA
```

| Status | Significado |
|--------|------------|
| `PENDENTE` | Cobrança iniciada na maquininha |
| `APROVADA` | Pagamento aprovado pelo Pagar.me |
| `RECUSADA` | Cartão recusado |
| `ESTORNADA` | Estorno após aprovação |

### Enum no backend
```java
public enum StatusTransacao {
    PENDENTE,
    APROVADA,
    RECUSADA,
    ESTORNADA
}
```

---

## Status da Liquidação Diária

```
PENDENTE ──────► PROCESSADA
    │
    └──────► FALHA
```

| Status | Significado |
|--------|------------|
| `PENDENTE` | Aguardando processamento (23:30) |
| `PROCESSADA` | Transferência feita via Pagar.me |
| `FALHA` | Erro na transferência |

### Enum no backend
```java
public enum StatusLiquidacao {
    PENDENTE,
    PROCESSADA,
    FALHA
}
```

---

## Correlação entre status

| Ação | Pedido | Entrega | Motoboy | Transação |
|------|--------|---------|---------|-----------|
| Loja cria pedido | `AGUARDANDO_ACEITE` | — | — | — |
| Backend cria entrega (auto) | — | `DISPONIVEL` | — | — |
| Motoboy aceita entrega | `ACEITO` | `ATRIBUIDA` | `EM_ENTREGA` | — |
| Admin atribui motoboy (alt.) | `ACEITO` | `ATRIBUIDA` | `EM_ENTREGA` | — |
| Motoboy confirma coleta | `COLETADO` | `COLETADA` | `EM_ENTREGA` | — |
| Motoboy cobra na maquininha | — | — | — | `APROVADA` |
| Motoboy finaliza entrega | `ENTREGUE` | `FINALIZADA` | `DISPONIVEL` | — |
| Liquidação diária (23:30) | — | — | — | `liquidada=true` |

