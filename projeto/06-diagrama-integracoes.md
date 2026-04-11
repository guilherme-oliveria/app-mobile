# 6 — Diagrama de Integrações Externas

> Todos os serviços externos que o backend consome e para que servem.

---

## Visão Geral das Integrações

```
┌────────────────────────────────────────────────────────────────────┐
│                        SEU BACKEND (Spring Boot)                    │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                     INTEGRAÇÕES                              │   │
│   │                                                              │   │
│   │  ┌─────────────┐    ┌──────────────┐    ┌───────────────┐   │   │
│   │  │  PAGAR.ME   │    │  FIREBASE    │    │   RABBITMQ    │   │   │
│   │  │  (Split $)  │    │  (Push FCM)  │    │   (Filas)     │   │   │
│   │  └──────┬──────┘    └──────┬───────┘    └──────┬────────┘   │   │
│   │         │                  │                    │            │   │
│   └─────────┼──────────────────┼────────────────────┼────────────┘   │
│             │                  │                    │                │
└─────────────┼──────────────────┼────────────────────┼────────────────┘
              │                  │                    │
              ▼                  ▼                    ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────────┐
│   PAGAR.ME API   │  │  FIREBASE CLOUD  │  │     RABBITMQ FILAS       │
│                  │  │  MESSAGING       │  │                          │
│ • Cadastrar      │  │                  │  │ fila.entrega             │
│   recebedor      │  │ • Push para      │  │   → nova entrega criada  │
│   (onboarding    │  │   motoboy:       │  │                          │
│    loja)         │  │   "Nova entrega  │  │ fila.notificacao         │
│                  │  │    atribuída"    │  │   → disparar push FCM    │
│ • Criar cobrança │  │                  │  │                          │
│   com split_rules│  │ • Push para      │  │ fila.pagamento           │
│   (maquininha)   │  │   loja:          │  │   → processar pagamento  │
│                  │  │   "Pedido        │  │     assincronamente      │
│ • Transferência  │  │    entregue"     │  │                          │
│   (liquidação    │  │                  │  │ Desacopla operações      │
│    D+1/D+2)      │  │ fcmToken salvo   │  │ pesadas do request HTTP  │
│                  │  │ em motoboys      │  │                          │
└──────────────────┘  └──────────────────┘  └──────────────────────────┘
```

---

## Detalhes por integração

### 1. Pagar.me (Processamento de Pagamento e Split)

| Método | Arquivo | Status | O que faz |
|--------|---------|--------|-----------|
| `criarTransacaoComSplit()` | `PagarmeService.java` | 🟡 Stub (TODO) | Cobra na maquininha com split automático |
| `criarTransferencia()` | `PagarmeService.java` | 🟡 Stub (TODO) | Transfere saldo líquido para conta da loja |
| `cadastrarRecebedor()` | `PagarmeService.java` | 🟡 Stub (TODO) | Registra loja como recebedor (onboarding) |

**API Base:** `https://api.pagar.me/core/v5`

**Fluxo de cobrança com split:**
```
POST /orders
{
  "items": [{"amount": 8990, "description": "Entrega", "quantity": 1}],
  "payments": [{
    "payment_method": "credit_card",
    "split": [
      {"recipient_id": "re_loja_abc", "type": "percentage", "amount": 93},
      {"recipient_id": "re_plataforma", "type": "percentage", "amount": 7}
    ]
  }]
}
```

### 2. Firebase Cloud Messaging (Push Notifications)

| Quando | Para quem | Mensagem |
|--------|-----------|----------|
| Entrega criada (DISPONIVEL) | TODOS motoboys disponíveis | "🔔 Nova entrega disponível! Loja X → Rua Y" |
| Admin atribui motoboy | Motoboy atribuído | "📦 Entrega atribuída a você! Cliente: Z" |
| Coleta confirmada | Loja | "Motoboy coletou o pedido #42" |
| Entrega finalizada | Loja | "Pedido #42 entregue ao cliente" |

**Configuração:**
- Backend: `firebase-credentials.json` (service account key)
- Motoboy App: `google-services.json` (Android)
- Store App: `google-services.json` (Android)
- O `fcmToken` é salvo na tabela `motoboys` pelo endpoint `PATCH /api/motoboys/{id}/fcm-token`

> Sem Firebase configurado, os apps funcionam normalmente — só não recebem push.

### 3. RabbitMQ (Filas de Mensageria)

| Fila | Produtor | Consumidor | Cenário |
|------|----------|------------|---------|
| `fila.notificacao` | Qualquer mudança de status | `EventConsumers.processarNotificacaoPush()` | Push FCM individual para motoboy |
| `fila.entrega.criada` | Quando entrega é criada | `EventConsumers.processarEntregaCriada()` | Notifica todos motoboys disponíveis + dashboard admin |
| `fila.entrega.aceita` | Motoboy aceita ou admin atribui | `EventConsumers.processarEntregaAceita()` | Atualiza dashboard + notifica motoboy (se admin atribuiu) |
| `fila.entrega.status` | Coleta, finalização, cancelamento | `EventConsumers.processarStatusAlterado()` | Atualiza dashboard admin em tempo real |
| `fila.pagamento` | Quando maquininha aprova | Listener que registra transação | Processamento assíncrono |

**Por que RabbitMQ?**
- Se o Firebase ou Pagar.me estiver fora, a mensagem fica na fila e é processada depois
- A resposta HTTP para o app é imediata (não espera o push ser enviado)
- Garantia de entrega: mensagem só sai da fila após confirmação de processamento

### 4. Redis (Cache)

| O que cachear | TTL sugerido | Por quê |
|---------------|-------------|---------|
| Lista de motoboys disponíveis | 30s | Consulta frequente no dashboard |
| Pedidos por status | 30s | Dashboard atualiza a cada poll |
| Token JWT válidos | até expiração | Evita consultar banco a cada request |

### 5. WebSocket (STOMP — Tempo Real)

| Tópico | Quem ouve | O que recebe |
|--------|-----------|-------------|
| `/topic/entregas` | Admin Angular (dashboard) | Novas entregas, mudanças de status |
| `/topic/pedidos` | Admin + Store App | Novos pedidos, atualizações |
| `/topic/motoboy/{id}` | Motoboy App específico | Entrega atribuída a ele |

**Configurado em:** `WebSocketConfig.java` (endpoint `/ws`, prefixo `/topic`)

---

## Resumo de Status

| Integração | Arquivo | Status |
|-----------|---------|--------|
| Pagar.me | `PagarmeService.java` | 🟡 Stub — métodos retornam IDs fake |
| Firebase FCM | `FirebaseNotificacaoService.java` | 🟡 Precisa configurar credentials |
| RabbitMQ | `EventConsumers.java` + `EntregaEventPublisher.java` | ✅ 4 consumers implementados |
| Redis | Docker compose + Spring Data Redis | ✅ Infra pronta |
| WebSocket | `WebSocketConfig.java` | ✅ Configurado e funcionando |

