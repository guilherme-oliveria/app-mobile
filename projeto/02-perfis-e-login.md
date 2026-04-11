# 2 — Perfis de Usuário (Roles) e Login

> Todo mundo faz login pela mesma API (`POST /api/auth/login`).
> O campo `role` diferencia o que cada um pode fazer.

---

## Tabela `usuarios` — Login unificado

| Role | Quem é | App que usa | `refId` aponta para |
|------|--------|-------------|---------------------|
| `ADMIN` | Você (administrador total) | Admin Angular | — (null) |
| `SUPORTE` | Operador de suporte | Admin Angular | — (null) |
| `LOJA` | Dono da loja | Store App (Flutter) | `lojas.id` |
| `MOTOBOY` | Entregador | Motoboy App (Flutter) | `motoboys.id` |

---

## Matriz de Permissões

| Funcionalidade | ADMIN | SUPORTE | LOJA | MOTOBOY |
|----------------|:-----:|:-------:|:----:|:-------:|
| **Lojas** | | | | |
| Listar todas as lojas | ✅ | ✅ | ❌ | ❌ |
| Ver uma loja | ✅ | ✅ | ✅ (própria) | ❌ |
| Cadastrar loja | ✅ | ✅ | ❌ | ❌ |
| Editar loja | ✅ | ✅ | ✅ (própria) | ❌ |
| **Inativar/excluir loja** | ✅ | ❌ | ❌ | ❌ |
| **Motoboys** | | | | |
| Listar todos os motoboys | ✅ | ✅ | ❌ | ❌ |
| Listar motoboys disponíveis | ✅ | ✅ | ✅ | ❌ |
| Ver um motoboy | ✅ | ✅ | ❌ | ✅ (próprio) |
| Cadastrar motoboy | ✅ | ✅ | ❌ | ❌ |
| Atualizar status motoboy | ✅ | ✅ | ❌ | ✅ (próprio) |
| **Inativar/excluir motoboy** | ✅ | ❌ | ❌ | ❌ |
| **Pedidos** | | | | |
| Ver pedidos (por loja/status) | ✅ | ✅ | ✅ (próprios) | ✅ |
| Criar pedido | ✅ | ❌ | ✅ | ❌ |
| Cancelar / alterar status | ✅ | ❌ | ✅ | ✅ |
| **Entregas** | | | | |
| Ver entregas disponíveis | ✅ | ✅ | ❌ | ✅ |
| Criar entrega para pedido | ✅ | ✅ | ✅ | ❌ |
| Atribuir motoboy (manual) | ✅ | ✅ | ❌ | ❌ |
| Aceitar entrega (auto) | ❌ | ❌ | ❌ | ✅ |
| Confirmar coleta / finalizar | ❌ | ❌ | ❌ | ✅ |
| **Financeiro** | | | | |
| Ver liquidações de lojas | ✅ | ❌ | ✅ (própria) | ❌ |
| Disparar liquidação manual | ✅ | ❌ | ❌ | ❌ |
| Processar pagamento | ❌ | ❌ | ❌ | ✅ |
| **Admin Angular — UI** | | | | |
| Menu Relatórios | ✅ | ❌ | — | — |
| Botão "Inativar" lojas/motoboys | ✅ | ❌ | — | — |

---

## Estrutura da entidade Usuario

```
Usuario
├── id (PK)
├── email (UNIQUE) ← usado como login
├── senha (AES-256-GCM) ← encriptada com passphrase reversível
├── nome
├── role (ENUM: ADMIN | SUPORTE | LOJA | MOTOBOY)
├── refId ← referência para loja.id ou motoboy.id (null para ADMIN/SUPORTE)
├── ativo (boolean)
└── criadoEm (timestamp)
```

---

## Fluxo de Autenticação

```
┌──────────┐     POST /api/auth/login           ┌──────────────┐
│  App     │ ──────────────────────────────────► │   Backend    │
│ (qualquer│     { email, senha }               │              │
│  role)   │                                    │  AuthManager │
│          │ ◄────────────────────────────────── │  + JwtService│
│          │  { token, role, nome, refId }       │              │
└──────────┘                                    └──────────────┘
     │
     │  JWT armazenado:
     │  - Angular: localStorage
     │  - Flutter: FlutterSecureStorage
     │
     │  A partir daqui, TODA request envia:
     │  Header: Authorization: Bearer <token>
     │
     ▼
┌──────────────────────────────────────────────────────────┐
│  JwtAuthFilter (Spring Security)                         │
│  Intercepta TODA request:                               │
│  1. Extrai token do header                              │
│  2. Valida assinatura + expiração                       │
│  3. Extrai email + role                                 │
│  4. Cria SecurityContext                                │
│  5. @PreAuthorize verifica permissão por endpoint       │
└──────────────────────────────────────────────────────────┘
```

---

## JWT — Estrutura do Token

```json
{
  "sub": "suporte@delivery.com",
  "role": "SUPORTE",
  "refId": null,
  "iat": 1712000000,
  "exp": 1712086400
}
```

| Campo | Descrição |
|-------|-----------|
| `sub` | Email do usuário (subject) |
| `role` | Perfil (`ADMIN`, `SUPORTE`, `LOJA`, `MOTOBOY`) |
| `refId` | ID da loja ou motoboy (`null` para ADMIN e SUPORTE) |
| `iat` | Issued at (quando foi gerado) |
| `exp` | Expiração (24h padrão) |

---

## Criação de Credenciais

### Admin e Suporte (Flyway seed — migrations V2 e V3)

Senhas encriptadas com **AES-256-GCM** (passphrase configurada em `crypto.passphrase`).
Para descriptografar, use a classe `CryptoUtil.java` (opção 2 do menu).

```sql
-- V2__seed_usuario_admin.sql → admin@delivery.com (role ADMIN)
-- V3__seed_usuario_suporte.sql → suporte@delivery.com (role SUPORTE)
-- ⚠️ Trocar senhas e passphrase em produção!
```

### Loja e Motoboy (criados pelo Admin ou Suporte)
1. Admin/Suporte cadastra a loja/motoboy via painel
2. Admin/Suporte cria um `Usuario` vinculando: `role=LOJA, refId=loja.id` ou `role=MOTOBOY, refId=motoboy.id`
3. Loja/Motoboy faz login no app correspondente

> **Pendente:** automatizar a criação do `Usuario` junto com o cadastro da loja/motoboy.

