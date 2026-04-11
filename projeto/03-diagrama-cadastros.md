# 3 — Diagrama de Cadastros (Quem Cadastra Quem)

> O Admin é o único que pode criar lojas e motoboys na plataforma.

---

## Fluxo de Cadastro

```
                    ┌─────────────────┐
                    │   ADMIN (Você)  │
                    │  Angular Panel  │
                    └────────┬────────┘
                             │
               ┌─────────────┼──────────────┐
               │             │               │
               ▼             ▼               ▼
        ┌──────────┐  ┌──────────┐    ┌──────────────┐
        │ Cadastra  │  │ Cadastra │    │  Gerencia    │
        │  LOJAS    │  │ MOTOBOYS │    │  PEDIDOS     │
        │           │  │          │    │  ENTREGAS    │
        │ • nome    │  │ • nome   │    │  RELATÓRIOS  │
        │ • cnpj    │  │ • cpf    │    │              │
        │ • email   │  │ • email  │    │ • dashboard  │
        │ • tel     │  │ • tel    │    │ • atribuir   │
        │ • endereço│  │ • cnh    │    │   motoboy    │
        │ • chavePix│  │ • serial │    │ • liquidação │
        │           │  │   smart  │    │              │
        │ POST      │  │   POS    │    │              │
        │ /api/lojas│  │          │    │              │
        └──────────┘  │ POST     │    └──────────────┘
                      │/api/     │
                      │motoboys  │
                      └──────────┘
```

---

## Cadastrar Loja — Passo a passo

| Etapa | Quem | Ação | Endpoint |
|-------|------|------|----------|
| 1 | Admin | Acessa `/lojas` no painel Angular | — |
| 2 | Admin | Clica "Nova Loja" | — |
| 3 | Admin | Preenche: nome, CNPJ, email, telefone, endereço, chave Pix | — |
| 4 | Backend | Cria registro na tabela `lojas` | `POST /api/lojas` |
| 5 | Backend | (TODO) Registra como recebedor no Pagar.me | `PagarmeService.cadastrarRecebedor()` |
| 6 | Admin | Cria `Usuario` com `role=LOJA` e `refId=loja.id` | (manual / automático futuro) |
| 7 | Loja | Instala Store App e faz login | `POST /api/auth/login` |

### Dados obrigatórios da Loja

```
Loja
├── nome        ← Nome fantasia
├── cnpj        ← CNPJ (UNIQUE, validar formato)
├── email       ← Email (UNIQUE, usado para login)
├── telefone    ← Contato
├── endereco    ← Endereço de coleta
├── chavePix    ← Para receber liquidação
└── pagarmeRecipientId ← Gerado pelo Pagar.me no onboarding
```

---

## Cadastrar Motoboy — Passo a passo

| Etapa | Quem | Ação | Endpoint |
|-------|------|------|----------|
| 1 | Admin | Acessa `/motoboys` no painel Angular | — |
| 2 | Admin | Clica "Novo Motoboy" | — |
| 3 | Admin | Preenche: nome, CPF, email, telefone, CNH, serial da maquininha | — |
| 4 | Backend | Cria registro na tabela `motoboys` | `POST /api/motoboys` |
| 5 | Admin | Cria `Usuario` com `role=MOTOBOY` e `refId=motoboy.id` | (manual / automático futuro) |
| 6 | Motoboy | Instala APK e faz login | `POST /api/auth/login` |

### Dados obrigatórios do Motoboy

```
Motoboy
├── nome              ← Nome completo
├── cpf               ← CPF (UNIQUE)
├── email             ← Email (UNIQUE, usado para login)
├── telefone          ← Contato
├── cnh               ← CNH (opcional mas recomendado)
├── smartPosSerial    ← Serial da maquininha atribuída (UNIQUE)
├── fcmToken          ← Token Firebase (atualizado pelo app)
├── latitudeAtual     ← GPS em tempo real (atualizado pelo app)
├── longitudeAtual    ← GPS em tempo real (atualizado pelo app)
└── status            ← DISPONIVEL | EM_ENTREGA | INATIVO
```

---

## Vinculação Usuario ↔ Entidade

```
                ┌────────────────────────────────────────────┐
                │ Admin cria MANUALMENTE o registro em       │
                │ "usuarios" vinculando:                     │
                │                                            │
                │   Loja    → role=LOJA,    refId=loja.id    │
                │   Motoboy → role=MOTOBOY, refId=motoboy.id│
                │                                            │
                │ (pendente: automatizar no onboarding)      │
                └────────────────────────────────────────────┘
```

### Exemplo: cadastro completo de uma nova loja

```sql
-- 1. Loja criada via POST /api/lojas → retorna id=5
-- 2. Criar usuario vinculado:
INSERT INTO usuarios (email, senha, nome, role, ref_id, ativo, criado_em)
VALUES (
  'pizzaria@email.com',
  '$2a$10$...hash_bcrypt...',
  'Pizzaria do João',
  'LOJA',
  5,        -- refId = loja.id
  true,
  NOW()
);
```

