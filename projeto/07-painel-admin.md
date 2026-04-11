# 7 — Painel Admin (Telas e Funcionalidades)

> Tudo que o Administrador vê e faz no painel Angular.

---

## Mapa completo de telas

```
┌───────────────────────────────────────────────────────────────┐
│                    PAINEL ADMIN (Angular)                       │
│                    http://localhost:4200                         │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  🔐 /login (LoginComponent)                                   │
│  └── Email + senha → POST /api/auth/login → JWT salvo         │
│                                                               │
│  📊 /dashboard (DashboardComponent)                           │
│  ├── Cards: pedidos aguardando | entregas disponíveis | motoboys│
│  └── Tabela: entregas DISPONÍVEIS → motoboy aceita ou admin atribui │
│                                                               │
│  📋 /pedidos (PedidosComponent)                               │
│  ├── Lista todos os pedidos (todas as lojas)                  │
│  ├── Filtro por status                                        │
│  └── Botão "Criar Entrega" → gera entrega para o pedido      │
│                                                               │
│  🏪 /lojas (LojasComponent)                                   │
│  ├── CRUD de lojas (criar, listar)                            │
│  └── Colunas: id, nome, cnpj, email, tel, saldo, status      │
│                                                               │
│  🛵 /motoboys (MotoboysComponent)                              │
│  ├── CRUD de motoboys (criar, listar)                         │
│  └── Colunas: id, nome, cpf, email, tel, serial POS, status  │
│                                                               │
│  📈 /relatorios (RelatoriosComponent)                          │
│  ├── Seleciona loja → mostra liquidações diárias              │
│  ├── Totais: entregas, bruto, taxas, líquido                  │
│  └── Botão "Liquidar agora" (dev/teste)                       │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

---

## Rotas (app.routes.ts)

```
/                    → redireciona para /dashboard
/login               → LoginComponent (sem guard)
/dashboard           → DashboardComponent (authGuard)
/pedidos             → PedidosComponent (authGuard)
/motoboys            → MotoboysComponent (authGuard)
/lojas               → LojasComponent (authGuard)
/relatorios          → RelatoriosComponent (authGuard)
/**                  → redireciona para /dashboard
```

Todas as rotas internas ficam dentro do `LayoutComponent` (sidebar + header).

---

## Detalhes de cada tela

### 🔐 Login

- Campos: email, senha
- Chama `POST /api/auth/login`
- Recebe JWT + role + nome
- JWT salvo em `localStorage`
- `AuthInterceptor` adiciona `Authorization: Bearer <token>` em toda request
- `authGuard` protege todas as rotas internas

### 📊 Dashboard

- **3 cards de métricas:**
  - Pedidos aguardando aceite (`GET /api/pedidos/status/AGUARDANDO_ACEITE`)
  - Entregas disponíveis sem motoboy (`GET /api/entregas/disponiveis`)
  - Motoboys disponíveis (`GET /api/motoboys/disponiveis`)

- **Tabela de entregas disponíveis (modelo híbrido):**
  - Mostra entregas com status `DISPONIVEL` (nenhum motoboy aceitou ainda)
  - Colunas: #, Cliente, Endereço, Valor, Tempo esperando, Ação
  - Ação: dropdown com motoboys disponíveis → atribuição manual
  - Ao selecionar → `PATCH /api/entregas/{id}/atribuir` com `{motoboyId}`
  - Se motoboy já aceitou sozinho, entrega sai da lista automaticamente
  - Atualiza em tempo real via WebSocket
  - ⚠️ Highlight em vermelho se entrega está disponível há mais de 10 min

### 📋 Pedidos

- Lista todos os pedidos de todas as lojas
- Carrega por status: AGUARDANDO_ACEITE, ACEITO, COLETADO, EM_ENTREGA, ENTREGUE
- **Filtro por status** (mat-select)
- **Colunas:** #, Loja, Cliente, Endereço, Valor, Status (colorido), Data, Ações
- **Ação:** Botão "Criar Entrega" → `POST /api/entregas/pedido/{id}`

### 🏪 Lojas

- **Listar:** tabela com id, nome, cnpj, email, telefone, saldo pendente, status
- **Criar:** formulário com nome, cnpj, email (obrigatórios)
- Endpoint: `GET /api/lojas` e `POST /api/lojas`

### 🛵 Motoboys

- **Listar:** tabela com id, nome, cpf, email, telefone, serial smart POS, status
- **Criar:** formulário com nome, cpf, email (obrigatórios)
- Endpoint: `GET /api/motoboys` e `POST /api/motoboys`

### 📈 Relatórios

- **Seleciona loja** (dropdown) → carrega liquidações
- **Tabela:** data, total entregas, valor bruto, taxas, valor líquido, status
- **Footer com totais:** soma de entregas, bruto, taxas e líquido
- **Botão "Liquidar agora"** → `POST /api/pagamentos/liquidar-agora` (uso em dev)
- Endpoint: `GET /api/pagamentos/liquidacoes/loja/{id}`

---

## Tecnologias da UI

| Componente | Biblioteca |
|-----------|-----------|
| Tabelas | `MatTableModule` |
| Cards | `MatCardModule` |
| Formulários | `MatFormFieldModule` + `MatInputModule` |
| Dropdowns | `MatSelectModule` |
| Botões | `MatButtonModule` |
| Ícones | `MatIconModule` (Material Icons) |
| Layout sidebar | CSS custom + `LayoutComponent` |

---

## Serviço de API (api.service.ts)

Todas as chamadas HTTP ficam centralizadas no `ApiService`:

```
ApiService
├── Lojas
│   ├── getLojas() → GET /lojas
│   ├── getLoja(id) → GET /lojas/{id}
│   └── criarLoja(data) → POST /lojas
├── Motoboys
│   ├── getMotoboys() → GET /motoboys
│   ├── getMotoboysdisponiveis() → GET /motoboys/disponiveis
│   └── criarMotoboy(data) → POST /motoboys
├── Pedidos
│   ├── getPedidosPorLoja(id) → GET /pedidos/loja/{id}
│   ├── getPedidosPorStatus(status) → GET /pedidos/status/{status}
│   └── atualizarStatusPedido(id, status) → PATCH /pedidos/{id}/status
├── Entregas
│   ├── getEntregasDisponiveis() → GET /entregas/disponiveis
│   ├── atribuirMotoboy(entregaId, motoboyId) → PATCH /entregas/{id}/atribuir
│   └── criarEntrega(pedidoId) → POST /entregas/pedido/{id}
└── Pagamentos
    ├── getLiquidacoesPorLoja(id) → GET /pagamentos/liquidacoes/loja/{id}
    └── liquidarAgora() → POST /pagamentos/liquidar-agora
```

