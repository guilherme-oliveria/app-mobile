# 12 — Pendências e Roadmap para Produção

> O que já está pronto e o que falta para ir para produção.

---

## Status Atual ✅

| Item | Status | Detalhes |
|------|--------|---------|
| Estrutura monorepo | ✅ Pronto | backend + admin + mobile |
| Docker Compose | ✅ Pronto | PostgreSQL 17 + Redis 7 + RabbitMQ 3 |
| Backend Spring Boot | ✅ Pronto | Auth, Pedido, Entrega, Pagamento, Motoboy, Loja |
| Flyway migrations | ✅ Pronto | V1 (tabelas) + V2 (admin) + V3 (suporte) |
| Spring Security + JWT | ✅ Pronto | Login unificado + RBAC (ADMIN, SUPORTE, LOJA, MOTOBOY) |
| Criptografia AES-256-GCM | ✅ Pronto | Senhas encriptadas no banco com passphrase reversível |
| Admin Angular 21 | ✅ Pronto | Dashboard, Pedidos, Motoboys, Lojas, Relatórios |
| Angular Material | ✅ Pronto | Todas as telas com Material components |
| Flutter motoboy_app | ✅ Pronto | Login, Home (tabs Disponíveis/Minhas), Entrega Ativa |
| Flutter store_app | ✅ Pronto | Login, Pedidos (tabs Ativos/Todos) |
| WebSocket (STOMP) | ✅ Pronto | Tempo real no dashboard admin |
| RabbitMQ consumers | ✅ Pronto | 4 consumers: push FCM, entrega criada/aceita, status |
| Modelo híbrido de atribuição | ✅ Pronto | Auto-atribuição por motoboy + intervenção do admin |
| Optimistic locking | ✅ Pronto | `@Version` na entrega → evita race condition entre motoboys |
| Timeout de entregas | ✅ Pronto | Job agendado a cada 2min → alerta admin se sem aceite há 10min |
| GlobalExceptionHandler | ✅ Pronto | Trata 409 (optimistic lock), 422 (negócio), 404 (não encontrado) |
| Perfil SUPORTE | ✅ Pronto | Acesso operacional sem permissões financeiras/exclusão |
| Debug remoto (Docker) | ✅ Pronto | Backend JDWP:5005 + Angular source maps |
| Dockerfiles multi-stage | ✅ Pronto | Backend, Admin, Mobile |
| Documentação | ✅ Completa | INSTRUCTIONS.md + README + /projeto (13 documentos) |

---

## Pendências para Produção 🔜

### 🔴 Prioridade Alta

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| Automatizar criação de `Usuario` ao cadastrar loja/motoboy | `LojaService.criar()` e `MotoboyService.criar()` | Hoje admin precisa criar manualmente no banco |
| Implementar chamadas reais ao Pagar.me (tirar stubs) | `PagarmeService.java` (3 métodos com TODO) | Sem isso, não processa pagamento real |
| Integrar SDK Stone Smart POS no Flutter | `entrega_ativa_screen.dart` (linha com TODO) | Sem isso, não cobra na maquininha |
| Configurar Firebase FCM | `google-services.json` + `firebase-credentials.json` | Sem isso, sem push notifications |

### 🟡 Prioridade Média

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| Testes automatizados | JUnit + Mockito + WebMvcTest | Qualidade de código |
| Webhook Pagar.me | Novo endpoint `POST /api/webhooks/pagarme` | Confirmação assíncrona de pagamento |
| Tela de criação de pedidos no store_app | `store_app/lib/screens/novo_pedido_screen.dart` | Hoje só lista pedidos |
| Validações de formulário (frontend) | Todos os componentes com form | Validar CNPJ, CPF, email, etc. |

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

## 📱 Distribuição Mobile

### Android (.APK) — ✅ Pronto (Windows/Linux/Mac)

```bash
# Gera APKs de ambos os apps via Docker
docker compose --profile build run --rm flutter-builder

# Output: mobile/apk-output/
#   ├── motoboy_app.apk
#   └── store_app.apk
```

**Distribuir via:** Play Store (oficial) ou link direto para download.

### iOS (.IPA) — ⏳ Requer Mac

**Mesmo código Flutter**, muda apenas a plataforma de build:

```bash
# No Mac, dentro da pasta mobile/motoboy_app:
flutter build ios --release

# Gera: build/ios/ipa/motoboy_app.ipa
```

| | Android | iOS |
|---|---|---|
| Plataforma build | ✅ Windows/Linux/Docker | ❌ **Só Mac + Xcode** |
| Arquivo gerado | `.apk` | `.ipa` |
| Código Flutter | ✅ **100% reutilizável** | ✅ **100% reutilizável** |
| Custo publicação | $25 (única vez — Play Store) | $99/ano (Apple Developer) |

---

## Checklist pré-produção

```
[x] Flyway migrations versionadas (V1 tabelas + V2 admin + V3 suporte)
[x] GlobalExceptionHandler com tratamento de 409/422/404/500
[x] Modelo híbrido de atribuição com optimistic locking
[x] RabbitMQ consumers para FCM, WebSocket, entregas
[x] Timeout de entregas sem aceite (job agendado)
[x] Perfil SUPORTE com permissões restritas
[x] Debug remoto habilitado (JDWP:5005 + source maps Angular)
[ ] Trocar jwt.secret para uma chave forte (min 256 bits)
[ ] Trocar credenciais do banco (não usar postgres/postgres)
[ ] Trocar crypto.passphrase via variável de ambiente segura
[ ] Configurar HTTPS (SSL/TLS) no backend
[ ] Configurar CORS para domínio real (não localhost)
[ ] Remover debug remoto do backend (Dockerfile ENTRYPOINT + porta 5005)
[ ] Remover source maps do Angular (Dockerfile: --configuration production)
[ ] Remover botão "Liquidar agora" ou proteger em produção
[ ] Configurar backup automático do PostgreSQL
[ ] Configurar monitoramento (Spring Actuator + Prometheus/Grafana)
[ ] Testar fluxo completo end-to-end
[ ] Consultar advogado sobre contratos (loja + motoboy)
[ ] Obter CNPJ com CNAEs corretos
[ ] Configurar conta Pagar.me de produção
[ ] Configurar Firebase FCM (google-services.json + firebase-credentials.json)
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

