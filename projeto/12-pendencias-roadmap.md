# 12 — Pendências e Roadmap para Produção

> O que já está pronto e o que falta para ir para produção.

---

## Status Atual ✅

| Item | Status | Detalhes |
|------|--------|---------|
| Estrutura monorepo | ✅ Pronto | backend + admin + mobile |
| Docker Compose | ✅ Pronto | PostgreSQL 17 + Redis 7 + RabbitMQ 3 |
| Backend Spring Boot | ✅ Pronto | Auth, Pedido, Entrega, Pagamento, Motoboy, Loja |
| Flyway migrations | ✅ Pronto | V1 (tabelas) + V2 (admin) + V3 (suporte) + V4 (deve_alterar_senha) + V5 (atualizado_em) |
| Spring Security + JWT | ✅ Pronto | Login unificado + RBAC (ADMIN, SUPORTE, LOJA, MOTOBOY) |
| Criptografia AES-256-GCM | ✅ Pronto | Senhas encriptadas no banco com passphrase reversível |
| Criação automática de Usuário | ✅ Pronto | LojaService/MotoboyService criam Usuario + senha ao cadastrar |
| Troca de senha (apps mobile) | ✅ Pronto | Tela obrigatória no 1º login + menu "Trocar Senha" |
| Troca de senha (backend) | ✅ Pronto | `POST /api/auth/trocar-senha` com validação de senha atual |
| Admin Angular 21 | ✅ Pronto | Dashboard, Pedidos, Motoboys, Lojas, Relatórios |
| Angular Material | ✅ Pronto | Todas as telas com Material components |
| Validação visual nos formulários | ✅ Pronto | Campos obrigatórios com `*`, `mat-error` por campo (lojas + motoboys) |
| Flutter motoboy_app | ✅ Pronto | Login, Home (tabs Disponíveis/Minhas), Entrega Ativa, Trocar Senha |
| Flutter store_app | ✅ Pronto | Login, Pedidos (tabs Ativos/Todos), Trocar Senha |
| Validação de perfil nos apps | ✅ Pronto | motoboy_app aceita MOTOBOY+ADMIN, store_app aceita LOJA+ADMIN |
| Detecção automática de ambiente | ✅ Pronto | `kIsWeb` → localhost (browser) / 10.0.2.2 (emulador Android) |
| WebSocket (STOMP) | ✅ Pronto | Tempo real no dashboard admin |
| RabbitMQ consumers | ✅ Pronto | 4 consumers: push FCM, entrega criada/aceita, status |
| Modelo híbrido de atribuição | ✅ Pronto | Auto-atribuição por motoboy + intervenção do admin |
| Optimistic locking | ✅ Pronto | `@Version` na entrega → evita race condition entre motoboys |
| Timeout de entregas | ✅ Pronto | Job agendado a cada 2min → alerta admin se sem aceite há 10min |
| GlobalExceptionHandler | ✅ Pronto | Trata 409/422/404/500 + extrai nome da coluna em erros not-null |
| Perfil SUPORTE | ✅ Pronto | Acesso operacional sem permissões financeiras/exclusão |
| CORS dinâmico | ✅ Pronto | `allowedOriginPatterns` com wildcard para dev (localhost:*) |
| `@Builder.Default` em todas as entidades | ✅ Pronto | Fix Lombok: Usuario, Loja, Motoboy, Entrega, Pedido, Transacao, LiquidacaoDia |
| `atualizadoEm` + `@PreUpdate` | ✅ Pronto | Coluna `atualizado_em` em usuarios, lojas, motoboys (V5) |
| Auth interceptor (Angular) | ✅ Pronto | Logout automático em 401 + validação JWT expirado no frontend |
| Firebase desabilitado para dev | ✅ Pronto | Comentado nos pubspec.yaml e main.dart — app funciona sem FCM |
| Debug remoto (Docker) | ✅ Pronto | Backend JDWP:5005 + Angular source maps |
| Dockerfiles multi-stage | ✅ Pronto | Backend, Admin, Mobile |
| Documentação | ✅ Completa | INSTRUCTIONS.md + README + /projeto (13 documentos) |

---

## Pendências para Produção 🔜

### 🔴 Prioridade Alta

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| Implementar chamadas reais ao Pagar.me (tirar stubs) | `PagarmeService.java` (3 métodos com TODO) | Sem isso, não processa pagamento real |
| Integrar SDK Stone Smart POS no Flutter | `entrega_ativa_screen.dart` (linha com TODO) | Sem isso, não cobra na maquininha |
| Configurar Firebase FCM | `google-services.json` + `firebase-credentials.json` | Sem isso, sem push notifications |
| Descomentar Firebase nos apps | `main.dart` + `pubspec.yaml` (motoboy_app + store_app) | Comentado para dev — precisa reativar para produção |

### 🟡 Prioridade Média

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| Testes automatizados | JUnit + Mockito + WebMvcTest | Qualidade de código |
| Webhook Pagar.me | Novo endpoint `POST /api/webhooks/pagarme` | Confirmação assíncrona de pagamento |
| Tela de criação de pedidos no store_app | `store_app/lib/screens/novo_pedido_screen.dart` | Hoje só lista pedidos |
| Validações de formulário (frontend) | Validar CNPJ, CPF, email no Angular e Flutter | Hoje valida apenas campos vazios |
| Tela de edição de loja/motoboy | Admin Angular | Hoje só cria e inativa, não edita pela UI |
| Configurar `--dart-define` para URL da API | `auth_service.dart` / `entrega_service.dart` | Não precisar editar código ao trocar ambiente |

### 🟢 Prioridade Baixa (melhorias)

| Item | Onde mexer | Por quê |
|------|-----------|---------|
| CI/CD pipeline | `.github/workflows/ci.yml` | Build + testes automáticos no push |
| Deploy em produção | VPS / Cloud (AWS, DigitalOcean) | Ir para o ar |
| Refresh token | `AuthController` + `JwtService` | Melhor UX (sem relogin a cada 24h) |
| Rate limiting | `SecurityConfig` ou API Gateway | Segurança contra abuso |
| Audit log | Nova tabela `audit_logs` | Rastreabilidade |
| Mapa de rastreio em tempo real | Admin dashboard + Google Maps API | Visualizar motoboy no mapa |
| Dark mode no admin | Angular Material theming | Conforto visual |

---

## 📱 Distribuição Mobile

### Motoboy App — roda no celular E na Smart POS

O **motoboy_app** é o mesmo APK para celular comum e Smart POS (Stone).
A Smart POS roda Android — o APK Flutter funciona direto nela.
O fluxo de pagamento na maquininha precisa do **SDK Stone** integrado (pendente).

```
motoboy_app.apk
    ├── Celular comum → aceitar entregas, confirmar coleta
    └── Smart POS (Stone) → tudo acima + cobrar na maquininha
```

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
[x] Flyway migrations versionadas (V1 a V5)
[x] GlobalExceptionHandler com tratamento de 409/422/404/500 + extração de coluna
[x] Modelo híbrido de atribuição com optimistic locking
[x] RabbitMQ consumers para FCM, WebSocket, entregas
[x] Timeout de entregas sem aceite (job agendado)
[x] Perfil SUPORTE com permissões restritas
[x] Debug remoto habilitado (JDWP:5005 + source maps Angular)
[x] Criação automática de usuário ao cadastrar loja/motoboy
[x] Tela de troca de senha obrigatória no 1º login (motoboy_app + store_app)
[x] Menu "Trocar Senha" nos apps mobile (PopupMenu)
[x] Validação de perfil no login dos apps (role errado = mensagem clara)
[x] @Builder.Default em todas as entidades (fix Lombok + null constraints)
[x] atualizadoEm + @PreUpdate nas entidades de cadastro
[x] CORS com allowedOriginPatterns (wildcard para dev)
[x] Detecção automática browser/emulador (kIsWeb)
[x] Formulários com campos obrigatórios marcados (* e mat-error)
[x] Auth interceptor Angular com logout automático em 401
[x] Firebase desabilitado para desenvolvimento local
[ ] Trocar jwt.secret para uma chave forte (min 256 bits)
[ ] Trocar credenciais do banco (não usar postgres/postgres)
[ ] Trocar crypto.passphrase via variável de ambiente segura
[ ] Configurar HTTPS (SSL/TLS) no backend
[ ] Configurar CORS para domínio real (trocar allowedOriginPatterns → allowedOrigins fixo)
[ ] Remover debug remoto do backend (Dockerfile ENTRYPOINT + porta 5005)
[ ] Remover source maps do Angular (Dockerfile: --configuration production)
[ ] Remover botão "Liquidar agora" ou proteger em produção
[ ] Configurar backup automático do PostgreSQL
[ ] Configurar monitoramento (Spring Actuator + Prometheus/Grafana)
[ ] Descomentar Firebase nos apps (main.dart + pubspec.yaml)
[ ] Configurar Firebase FCM (google-services.json + firebase-credentials.json)
[ ] Integrar SDK Stone Smart POS no motoboy_app
[ ] Integrar API Pagar.me real (tirar stubs)
[ ] Criar recipientId no Pagar.me ao cadastrar loja (onboarding)
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

Mobile apps (motoboy_app + store_app)
    ├── APK via Play Store ou link direto
    ├── motoboy_app também instala na Smart POS (Stone)
    └── CORS não se aplica — apps nativos fazem HTTP direto
```

---

## Estimativa de esforço

| Fase | Itens | Esforço estimado |
|------|-------|-----------------|
| MVP funcional (sem pagamento real) | Testes básicos + validações + tela novo pedido | 1-2 semanas |
| Integração Pagar.me | Tirar stubs + webhook + testes + onboarding loja | 2-3 semanas |
| Integração Stone Smart POS | SDK Flutter + testes na maquininha | 2-3 semanas |
| Firebase + RabbitMQ | Push + listeners + descomentar Firebase | 1 semana |
| Deploy + CI/CD | Pipeline + servidor + SSL + domínio | 1 semana |
| **Total até produção** | — | **7-10 semanas** |

