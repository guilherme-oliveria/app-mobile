# 🎯 Agent Orquestrador — Delivery App (Smart POS)

> Agente raiz que conhece o projeto inteiro e delega para os agentes especializados.

---

## Identidade

Você é o agente orquestrador do **Delivery App**, uma plataforma de entrega com maquininha Smart POS.
Você conhece todos os módulos, a arquitetura e sabe direcionar tarefas para o agente correto.

---

## Arquitetura do Projeto

```
app-mobile/
├── backend/              ← Java 21 + Spring Boot 3.3 (API REST)
├── admin/admin-angular/  ← Angular 21 + Material (painel web admin)
├── mobile/
│   ├── motoboy_app/      ← Flutter 3.29 (app do motoboy / Smart POS)
│   └── store_app/        ← Flutter 3.29 (app da loja)
├── docker-compose.yml    ← PostgreSQL 17 + Redis 7 + RabbitMQ 3
└── projeto/              ← Documentação (13 arquivos .md)
```

Todos os frontends (Angular + 2 Flutter) consomem a **mesma API REST** no backend.

---

## Stack Completa

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| Backend | Java + Spring Boot + Spring Security + JPA + Flyway | 21 / 3.3 |
| Admin | Angular + Angular Material + TypeScript | 21 |
| Mobile | Flutter + Dart + Provider | 3.29 / >=3.3 |
| Banco | PostgreSQL | 17 |
| Cache | Redis | 7 |
| Fila | RabbitMQ | 3 |
| Auth | JWT + AES-256-GCM (senhas reversíveis) | — |
| Pagamento | Pagar.me v5 (stub — não integrado ainda) | — |
| Push | Firebase FCM (desabilitado para dev) | — |

---

## Regras de Negócio Fundamentais

1. **Perfis:** ADMIN, SUPORTE, LOJA, MOTOBOY
2. **Cadastro:** Admin cadastra loja/motoboy → sistema cria `Usuario` automaticamente com `deveAlterarSenha=true`
3. **Login:** Endpoint único `POST /api/auth/login` → JWT com role + refId
4. **Validação de perfil nos apps:** motoboy_app aceita MOTOBOY+ADMIN, store_app aceita LOJA+ADMIN, SUPORTE só painel web
5. **Modelo híbrido de atribuição:** Motoboy se auto-atribui OU admin atribui. Optimistic locking com `@Version`
6. **Split de pagamento:** 5% plataforma + 2.5% gateway, resto para loja (via Pagar.me — stub)
7. **Troca de senha obrigatória** no 1º login nos apps mobile
8. **CORS:** `allowedOriginPatterns` com wildcard em dev; apps nativos (APK) não passam por CORS

---

## Delegação de Tarefas

| Tipo de tarefa | Delegue para |
|----------------|-------------|
| API REST, auth, entidades, migrations, services Java | **backend/AGENTS.md** |
| Painel admin, telas Angular, formulários, interceptors | **admin/admin-angular/AGENTS.md** |
| App do motoboy, entregas, pagamento, Smart POS | **mobile/motoboy_app/AGENTS.md** |
| App da loja, pedidos, acompanhamento | **mobile/store_app/AGENTS.md** |
| Docker, compose, infra, deploy | Este agente (orquestrador) |
| Documentação (/projeto/*.md) | Este agente (orquestrador) |

---

## Convenções do Projeto

### Backend (Java)
- Records como DTOs (`public record XxxRequest(...)`)
- `@Builder.Default` obrigatório em campos com valor default nas entidades
- `@PreUpdate` para `atualizadoEm` em entidades de cadastro
- Flyway para migrations (nunca alterar V1-V5, sempre criar V6+)
- GlobalExceptionHandler trata todos os erros (409/422/404/500)
- `BusinessException` para erros de regra de negócio (422)

### Angular
- Standalone components (sem NgModules)
- Angular Material para UI
- Template HTML separado do .ts
- `@if` / `@for` (nova sintaxe Angular 17+)
- AuthInterceptor + ErrorInterceptor
- Tema claro forçado (tokens M3 sobrescritos no styles.css)

### Flutter
- Provider para state management
- `kIsWeb` para detecção automática de ambiente (localhost vs 10.0.2.2)
- FlutterSecureStorage para JWT
- Firebase desabilitado para dev (comentado no main.dart e pubspec.yaml)

### Docker
- Dev: infra no Docker, código local (`mvn spring-boot:run`, `ng serve`, `flutter run`)
- Prod: tudo no Docker via compose
- Nunca misturar — escolha um caminho

---

## Documentação de Referência

| Arquivo | Conteúdo |
|---------|----------|
| `.github/INSTRUCTIONS.md` | Documento completo do projeto (905 linhas) |
| `projeto/01-visao-geral.md` | Modelo de negócio |
| `projeto/04-ciclo-vida-pedido.md` | Fluxo completo do pedido |
| `projeto/05-diagrama-entidades.md` | Modelo do banco |
| `projeto/08-diagrama-permissoes.md` | Matriz de permissões por role |
| `projeto/12-pendencias-roadmap.md` | Status + pendências + checklist |
| `projeto/13-modelo-atribuicao-entregas.md` | Modelo híbrido de atribuição |

---

## Comandos Essenciais

```bash
# Infra
docker compose up -d postgres redis rabbitmq

# Backend (dev local)
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Admin (dev local)
cd admin/admin-angular && npm start

# Flutter
cd mobile/motoboy_app && flutter pub get && flutter run
cd mobile/store_app && flutter pub get && flutter run

# Build produção (Docker)
docker compose up -d --build backend admin
docker compose --profile build run --rm flutter-builder
```

