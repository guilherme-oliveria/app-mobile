# 12 — Pendências e Roadmap para Produção

> O que já está pronto e o que falta para ir para produção.

---

## Status Atual ✅

| Item | Status | Detalhes |
|------|--------|---------|
| Estrutura monorepo | ✅ Pronto | backend + admin + mobile |
| Docker Compose | ✅ Pronto | PostgreSQL 17 + Redis 7 + RabbitMQ 3 |
| Backend Spring Boot | ✅ Pronto | Auth, Pedido, Entrega, Pagamento, Motoboy, Loja |
| Flyway migrations | ✅ Configurado | `db/migration/` |
| Spring Security + JWT | ✅ Pronto | Login unificado + RBAC por role |
| Admin Angular 21 | ✅ Pronto | Dashboard, Pedidos, Motoboys, Lojas, Relatórios |
| Angular Material | ✅ Pronto | Todas as telas com Material components |
| Flutter motoboy_app | ✅ Pronto | Login, Home, Entrega Ativa (com diálogo de pagamento) |
| Flutter store_app | ✅ Pronto | Login, Pedidos (tabs Ativos/Todos) |
| WebSocket (STOMP) | ✅ Configurado | Tempo real no dashboard |
| Dockerfiles multi-stage | ✅ Pronto | Backend, Admin, Mobile |
| Documentação | ✅ Completa | INSTRUCTIONS.md + README + /projeto |

---

## Pendências para Produção 🔜

### 🔴 Prioridade Alta

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| Implementar modelo híbrido de atribuição | `EntregaService`, `EntregaController`, `home_screen.dart` | Motoboy aceita sozinho + admin intervém. Ver [13-modelo-atribuicao-entregas.md](./13-modelo-atribuicao-entregas.md) |
| Automatizar criação de `Usuario` ao cadastrar loja/motoboy | `LojaService.criar()` e `MotoboyService.criar()` | Hoje admin precisa criar manualmente no banco |
| Implementar chamadas reais ao Pagar.me (tirar stubs) | `PagarmeService.java` (3 métodos com TODO) | Sem isso, não processa pagamento real |
| Integrar SDK Stone Smart POS no Flutter | `entrega_ativa_screen.dart` (linha com TODO) | Sem isso, não cobra na maquininha |
| Configurar Firebase FCM | `google-services.json` + `firebase-credentials.json` | Sem isso, sem push notifications |
| Flyway migration V1 | `backend/src/main/resources/db/migration/V1__create_tables.sql` | Script SQL com todas as tabelas |

### 🟡 Prioridade Média

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| RabbitMQ listeners | Criar `@RabbitListener` classes | Hoje as filas existem mas não são consumidas |
| Testes automatizados | JUnit + Mockito + WebMvcTest | Qualidade de código |
| Webhook Pagar.me | Novo endpoint `POST /api/webhooks/pagarme` | Confirmação assíncrona de pagamento |
| Tela de criação de pedidos no store_app | `store_app/lib/screens/novo_pedido_screen.dart` | Hoje só lista pedidos |
| Validações de formulário (frontend) | Todos os componentes com form | Validar CNPJ, CPF, email, etc. |
| Tratamento de erros global | `@ControllerAdvice` no backend + interceptor Angular | Mensagens amigáveis |

### 🟢 Prioridade Baixa (melhorias)

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| CI/CD pipeline | `.github/workflows/ci.yml` | Build + testes automáticos no push |
| Deploy em produção | VPS / Cloud (AWS, DigitalOcean) | Ir para o ar |
| Refresh token | `AuthController` + `JwtService` | Melhor UX (sem relogin a cada 24h) |
| Rate limiting | `SecurityConfig` ou API Gateway | Segurança contra abuso |
| Audit log | Nova tabela `audit_logs` | Rastreabilidade |
| Mapa de rastreio em tempo real | Admin dashboard + Google Maps API | Visualizar motoboy no mapa |
| Tela de edição de loja/motoboy | Admin Angular | Hoje só cria, não edita pela UI |
| Dark mode no admin | Angular Material theming | Conforto visual |

---

## Checklist pré-produção

```
[ ] Trocar jwt.secret para uma chave forte (min 256 bits)
[ ] Trocar credenciais do banco (não usar postgres/postgres)
[ ] Configurar HTTPS (SSL/TLS) no backend
[ ] Configurar CORS para domínio real (não localhost)
[ ] Criar migration V1 com Flyway (não usar ddl-auto=update)
[ ] Remover botão "Liquidar agora" ou proteger em produção
[ ] Configurar backup automático do PostgreSQL
[ ] Configurar monitoramento (Spring Actuator + Prometheus/Grafana)
[ ] Testar fluxo completo end-to-end
[ ] Consultar advogado sobre contratos (loja + motoboy)
[ ] Obter CNPJ com CNAEs corretos
[ ] Configurar conta Pagar.me de produção
```

---

## Arquitetura de deploy sugerida

```
                        ┌──────────────────────────────┐
                        │        CLOUD / VPS            │
                        │                               │
Internet ──► Nginx ──►  │  ┌─────────┐  ┌───────────┐  │
             (reverse   │  │ Backend │  │   Admin   │  │
              proxy +   │  │  :8080  │  │  (Nginx)  │  │
              SSL)      │  │         │  │   :4200   │  │
                        │  └────┬────┘  └───────────┘  │
                        │       │                       │
                        │  ┌────┴──────────────────┐   │
                        │  │ PostgreSQL | Redis |   │   │
                        │  │ RabbitMQ              │   │
                        │  └───────────────────────┘   │
                        │                               │
                        └──────────────────────────────┘

Mobile apps → APK distribuído via Play Store ou link direto
```

---

## Estimativa de esforço

| Fase | Itens | Esforço estimado |
|------|-------|-----------------|
| MVP funcional (sem pagamento real) | Automatizar Usuario + testes básicos | 1-2 semanas |
| Integração Pagar.me | Tirar stubs + webhook + testes | 2-3 semanas |
| Integração Stone Smart POS | SDK Flutter + testes na maquininha | 2-3 semanas |
| Firebase + RabbitMQ | Push + listeners | 1 semana |
| Deploy + CI/CD | Pipeline + servidor | 1 semana |
| **Total até produção** | — | **7-10 semanas** |

