# 1 — Visão Geral do Negócio

> Delivery App (Smart POS) — Plataforma de entrega com maquininha inteligente.

---

## O que é

Plataforma que conecta **Lojas**, **Motoboys** e um **Administrador** para realizar entregas
com cobrança na porta do cliente via maquininha Smart POS. O sistema faz split de pagamento
automático via Pagar.me — **a plataforma nunca toca no dinheiro da loja**.

---

## Fluxo Geral

```
┌──────────────────────────────────────────────────────────────────────┐
│                      FLUXO GERAL DE NEGÓCIO                         │
│                                                                      │
│   LOJA ──────► cria pedido ──────► PLATAFORMA ──────► MOTOBOY       │
│   (Store App)                     (Admin + Backend)  (Motoboy App)  │
│                                                                      │
│   Cliente paga na entrega → Pagar.me faz SPLIT automático:          │
│      92,5% → Loja (direto na conta)                                 │
│       5,0% → Você (taxa plataforma)                                 │
│       2,5% → Pagar.me (taxa gateway)                                │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Os 3 Apps

| App | Tecnologia | Quem usa | O que faz |
|-----|-----------|----------|-----------|
| **Backend** | Java 21 + Spring Boot 3.3 | — | API REST central (todos os apps consomem a mesma API) |
| **Admin** | Angular 21 + Material | Administrador | Painel web: dashboard, CRUD lojas/motoboys, relatórios, atribuição de entregas |
| **Motoboy App** | Flutter 3.29 | Motoboy | App Android para Smart POS: ver entregas, confirmar coleta, cobrar na maquininha |
| **Store App** | Flutter 3.29 | Lojista | App Android/iOS: acompanhar pedidos, criar novos pedidos |

---

## Modelo Financeiro Resumido

| Para quem | % | Em R$ 100 | Como recebe |
|-----------|---|-----------|-------------|
| **Loja** | 92,5% | R$ 92,50 | Direto do Pagar.me → conta bancária (D+1 ou D+2) |
| **Você** (plataforma) | 5,0% | R$ 5,00 | Fica na sua conta Pagar.me |
| **Pagar.me** (gateway) | 2,5% | R$ 2,50 | Retido automaticamente |

> Definido em `PagamentoService.java`: `TAXA_PLATAFORMA = 0.05` e `TAXA_GATEWAY = 0.025`

