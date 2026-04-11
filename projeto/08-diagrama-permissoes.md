# 8 — Diagrama de Permissões (RBAC)

> Controle de acesso por Role — quem pode fazer o quê em cada endpoint.

---

## Como funciona

O controle de acesso usa **Spring Security** com `@PreAuthorize` nos controllers.
O JWT carrega o `role` do usuário, e o `JwtAuthFilter` popula o `SecurityContext` com essa informação.

```
Request HTTP
    │
    ▼
JwtAuthFilter
    │ extrai role do JWT
    ▼
SecurityContext (ROLE_ADMIN, ROLE_SUPORTE, ROLE_LOJA ou ROLE_MOTOBOY)
    │
    ▼
@PreAuthorize("hasRole('ADMIN')")  ← verifica antes de executar o método
    │
    ▼
Controller executa (ou retorna 403 Forbidden)
```

---

## Matriz Completa de Permissões

```
┌──────────────────────────────────────────────────────────────────────┐
│                    QUEM PODE FAZER O QUÊ                              │
├──────────────────────────────┬──────┬─────────┬──────┬───────────────┤
│ Endpoint                     │ADMIN │ SUPORTE │ LOJA │ MOTOBOY       │
├──────────────────────────────┼──────┼─────────┼──────┼───────────────┤
│ POST  /auth/login            │  ✅  │   ✅    │  ✅  │   ✅          │
├──────────────────────────────┼──────┼─────────┼──────┼───────────────┤
│ GET   /lojas                 │  ✅  │   ✅    │  ❌  │   ❌          │
│ POST  /lojas                 │  ✅  │   ✅    │  ❌  │   ❌          │
│ GET   /lojas/{id}            │  ✅  │   ✅    │  ✅  │   ❌          │
│ PUT   /lojas/{id}            │  ✅  │   ✅    │  ✅  │   ❌          │
│ DEL   /lojas/{id}            │  ✅  │   ❌    │  ❌  │   ❌          │
├──────────────────────────────┼──────┼─────────┼──────┼───────────────┤
│ GET   /motoboys              │  ✅  │   ✅    │  ❌  │   ❌          │
│ GET   /motoboys/disponiveis  │  ✅  │   ✅    │  ✅  │   ❌          │
│ GET   /motoboys/{id}         │  ✅  │   ✅    │  ❌  │   ✅          │
│ POST  /motoboys              │  ✅  │   ✅    │  ❌  │   ❌          │
│ PATCH /motoboys/{id}/local.  │  ❌  │   ❌    │  ❌  │   ✅          │
│ PATCH /motoboys/{id}/status  │  ✅  │   ✅    │  ❌  │   ✅          │
│ PATCH /motoboys/{id}/fcm     │  ❌  │   ❌    │  ❌  │   ✅          │
│ DEL   /motoboys/{id}         │  ✅  │   ❌    │  ❌  │   ❌          │
├──────────────────────────────┼──────┼─────────┼──────┼───────────────┤
│ GET   /pedidos/status/{s}    │  ✅  │   ✅    │  ✅  │   ❌          │
│ GET   /pedidos/loja/{id}     │  ✅  │   ✅    │  ✅  │   ❌          │
│ POST  /pedidos               │  ✅  │   ❌    │  ✅  │   ❌          │
│ PATCH /pedidos/{id}/status   │  ✅  │   ❌    │  ✅  │   ✅          │
├──────────────────────────────┼──────┼─────────┼──────┼───────────────┤
│ GET   /entregas/disponiveis  │  ✅  │   ✅    │  ❌  │   ✅          │
│ GET   /entregas/motoboy/{id} │  ✅  │   ✅    │  ❌  │   ✅          │
│ POST  /entregas/pedido/{id}  │  ✅  │   ✅    │  ✅  │   ❌          │
│ POST  /entregas/{id}/aceitar │  ❌  │   ❌    │  ❌  │   ✅          │
│ PATCH /entregas/{id}/atribuir│  ✅  │   ✅    │  ❌  │   ❌          │
│ PATCH /entregas/{id}/coletar │  ❌  │   ❌    │  ❌  │   ✅          │
│ PATCH /entregas/{id}/finaliz.│  ❌  │   ❌    │  ❌  │   ✅          │
├──────────────────────────────┼──────┼─────────┼──────┼───────────────┤
│ POST  /pagamentos/processar  │  ❌  │   ❌    │  ❌  │   ✅          │
│ GET   /pagamentos/liquidações│  ✅  │   ❌    │  ✅  │   ❌          │
│ POST  /pagamentos/liquidar   │  ✅  │   ❌    │  ❌  │   ❌          │
└──────────────────────────────┴──────┴─────────┴──────┴───────────────┘
```

---

## Resumo por Role

### 👑 ADMIN — Pode tudo (gestão completa)
- CRUD de lojas e motoboys (incluindo excluir/inativar)
- Ver todos os pedidos de todas as lojas
- Criar entregas e atribuir motoboys
- Ver relatórios financeiros
- Executar liquidação manual

### 🛡️ SUPORTE — Operacional (sem financeiro nem exclusão)
- Cadastrar e listar lojas e motoboys
- Ver e atribuir entregas manualmente
- Ver pedidos e entregas disponíveis
- **NÃO pode:** excluir lojas/motoboys, ver relatórios financeiros, liquidar

### 🏪 LOJA — Somente seus dados
- Ver/editar seus próprios dados (`/lojas/{id}`)
- Ver motoboys disponíveis (para acompanhar)
- Criar pedidos (`POST /pedidos`)
- Ver seus pedidos (`/pedidos/loja/{id}`)
- Criar entrega para seus pedidos
- Ver suas liquidações

### 🛵 MOTOBOY — Somente suas entregas
- Ver seus dados (`/motoboys/{id}`)
- Atualizar localização GPS
- Atualizar token FCM
- Mudar seu status (DISPONIVEL/INATIVO)
- Ver suas entregas (`/entregas/motoboy/{id}`)
- Confirmar coleta e finalizar entrega
- Processar pagamento na maquininha

---

## Segurança extra recomendada (pendente)

| Melhoria | Descrição |
|----------|-----------|
| Verificar ownership | Loja só pode ver seus próprios pedidos (hoje depende do `refId`) |
| Rate limiting | Limitar requests por IP/token para evitar abuso |
| Refresh token | Implementar refresh para não precisar relogar a cada 24h |
| Audit log | Registrar quem fez o quê e quando |

