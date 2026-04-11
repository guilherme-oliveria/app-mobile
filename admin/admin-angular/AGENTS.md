# 🖥️ Agent Admin Angular — Painel Web Administrativo

> Agente especialista no painel admin Angular 21 + Material do Delivery App.

---

## Identidade

Você é o agente responsável pelo **painel administrativo web** do Delivery App.
Seu domínio é Angular 21, Angular Material, TypeScript, standalone components e comunicação com a API REST do backend.

---

## Tech Stack

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Angular | 21 | Framework SPA (standalone components, sem NgModules) |
| Angular Material | 21 | UI components (M3, tema claro forçado) |
| TypeScript | 5.9 | Linguagem |
| RxJS | 7.8 | Reatividade |
| RxStomp | 2.x | WebSocket STOMP client |

---

## Estrutura de Pastas

```
admin/admin-angular/src/app/
├── app.component.ts / .html      ← Root component
├── app.config.ts                 ← provideRouter, provideHttpClient, interceptors
├── app.routes.ts                 ← Rotas lazy-loaded com guards
├── core/
│   ├── guards/
│   │   └── auth.guard.ts         ← Redireciona para /login se não autenticado
│   ├── interceptors/
│   │   ├── auth.interceptor.ts   ← Injeta Bearer token + logout em 401
│   │   └── error.interceptor.ts  ← SnackBar com erro detalhado (campos inválidos, etc.)
│   └── services/
│       ├── api.service.ts        ← Todas as chamadas HTTP à API REST
│       └── auth.service.ts       ← Login, logout, getToken, isAdmin, role$
├── modules/
│   ├── auth/                     ← LoginComponent
│   ├── dashboard/                ← DashboardComponent (resumo geral)
│   ├── layout/                   ← LayoutComponent (sidenav + toolbar)
│   ├── lojas/                    ← LojasComponent (CRUD + formulário com senhaInicial)
│   ├── motoboys/                 ← MotoboysComponent (CRUD + formulário com senhaInicial)
│   ├── pedidos/                  ← PedidosComponent (listagem + status)
│   └── relatorios/               ← RelatoriosComponent (liquidações)
├── shared/
│   └── models/
│       └── models.ts             ← Interfaces: Loja, Motoboy, Pedido, Entrega, ErrorResponse, etc.
└── environments/
    ├── environment.ts            ← apiUrl: 'http://localhost:8080/api'
    └── environment.prod.ts       ← apiUrl: '/api' (Nginx proxy)
```

---

## Convenções Obrigatórias

### Components
- **Standalone** — sem NgModules (`imports: [...]` direto no `@Component`)
- Template HTML em **arquivo separado** (`templateUrl: './xxx.component.html'`)
- CSS em arquivo separado (`styleUrl: './xxx.component.css'`)

### Sintaxe de Template (Angular 17+)
- Usar `@if` / `@else` / `@for` (nova sintaxe de controle de fluxo)
- **NÃO** usar `*ngIf`, `*ngFor` em código novo

### Formulários
- Campos obrigatórios marcados com `*` no label
- Usar `required`, `minlength` nos inputs + `#campo="ngModel"`
- `<mat-error>` com mensagem específica por validação
- Validação no `.ts` antes de chamar a API (reforço)

### UI / Tema
- Angular Material com tema **claro forçado** (tokens M3 sobrescritos no `styles.css`)
- `MatSnackBar` para feedback de sucesso/erro (nunca `alert()`)
- Tabelas com `mat-table`, cards com `mat-card`

### Interceptors
- `authInterceptor` → injeta `Authorization: Bearer <token>` + logout em 401
- `errorInterceptor` → exibe SnackBar com mensagem do `ErrorResponse` do backend
  - Se houver `campos[]`, monta lista: "campo: mensagem"

### Services
- `ApiService` centraliza TODAS as chamadas HTTP
- `AuthService` gerencia token (localStorage), login/logout, role
- `AuthService.isAdmin()` — verifica se role é ADMIN
- `AuthService.isAdminOrSuporte()` — verifica se ADMIN ou SUPORTE

### Guards
- `authGuard` protege todas as rotas exceto `/login`
- Redireciona para `/login` se não tiver token válido

---

## Models (interfaces TypeScript)

```typescript
// Sempre manter sincronizado com o backend
interface Loja {
  id, nome, cnpj, email, telefone, endereco, chavePix?, ativo, saldoPendente, senhaInicial?
}
interface Motoboy {
  id, nome, cpf, email, telefone, cnh?, smartPosSerial, status, ativo, senhaInicial?
}
interface Pedido { id, lojaId, lojaNome, clienteNome, ..., status, criadoEm }
interface Entrega { id, pedido, motoboy?, status, atribuidaEm?, ... }
interface ErrorResponse { status, erro, mensagem, campos?: CampoErro[], timestamp }
interface LoginResponse { token, role, nome, refId?, deveAlterarSenha? }
```

---

## Permissões por Role no Admin

| Funcionalidade | ADMIN | SUPORTE |
|---------------|-------|---------|
| Dashboard | ✅ | ✅ |
| Listar lojas/motoboys | ✅ | ✅ |
| Cadastrar lojas/motoboys | ✅ | ✅ |
| **Inativar** lojas/motoboys | ✅ | ❌ |
| Pedidos | ✅ | ✅ |
| Atribuir motoboy a entrega | ✅ | ✅ |
| Relatórios financeiros | ✅ | ❌ |
| Liquidar agora | ✅ | ❌ |

---

## Como rodar

```bash
# Dev local (recomendado)
cd admin/admin-angular
npm install
npm start
# http://localhost:4200

# Docker
docker compose up -d --build admin
# http://localhost:4200
```

