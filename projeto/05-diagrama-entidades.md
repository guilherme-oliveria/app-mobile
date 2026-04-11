# 5 — Diagrama de Entidades (Banco de Dados)

> Todas as tabelas do PostgreSQL com seus relacionamentos.

---

## Diagrama ER (Entity Relationship)

```
┌────────────────────┐       ┌─────────────────────┐
│     USUARIOS       │       │       LOJAS          │
├────────────────────┤       ├─────────────────────┤
│ id (PK)            │       │ id (PK)             │
│ email (UNIQUE)     │  ref  │ nome                │
│ senha (BCrypt)     │──────►│ cnpj (UNIQUE)       │
│ nome               │       │ email (UNIQUE)      │
│ role (ENUM)        │       │ telefone            │
│ ref_id ────────────┤       │ endereco            │
│ ativo              │       │ chave_pix           │
│ criado_em          │       │ pagarme_recipient_id│
└────────────────────┘       │ saldo_pendente      │
         │                   │ ativo               │
         │ ref               │ criado_em           │
         ▼                   └──────────┬──────────┘
┌────────────────────┐                  │ 1
│     MOTOBOYS       │                  │
├────────────────────┤                  │ N
│ id (PK)            │       ┌──────────▼──────────┐
│ nome               │       │      PEDIDOS         │
│ cpf (UNIQUE)       │       ├─────────────────────┤
│ email (UNIQUE)     │       │ id (PK)             │
│ telefone           │       │ loja_id (FK)────────┤
│ cnh                │       │ cliente_nome        │
│ smart_pos_serial   │       │ cliente_telefone    │
│ fcm_token          │       │ endereco_entrega    │
│ latitude_atual     │       │ latitude_entrega    │
│ longitude_atual    │       │ longitude_entrega   │
│ status (ENUM)      │       │ valor_total         │
│ ativo              │       │ taxa_entrega        │
│ criado_em          │       │ status (ENUM)       │
└────────┬───────────┘       │ observacao          │
         │                   │ criado_em           │
         │                   │ atualizado_em       │
         │                   └──────────┬──────────┘
         │                              │ 1:1
         │ N:1                          │
         │                   ┌──────────▼──────────┐      ┌──────────────────┐
         │                   │      ENTREGAS        │      │   ITENS_PEDIDO   │
         │                   ├─────────────────────┤      ├──────────────────┤
         └──────────────────►│ id (PK)             │      │ id (PK)          │
                             │ pedido_id (FK,UNQ)  │      │ pedido_id (FK)   │
                             │ motoboy_id (FK)     │      │ descricao        │
                             │ status (ENUM)       │      │ quantidade       │
                             │ atribuida_em        │      │ valor_unitario   │
                             │ coletada_em         │      │ valor_total      │
                             │ finalizada_em       │      └──────────────────┘
                             │ codigo_confirmacao  │
                             │ criado_em           │
                             └──────────┬──────────┘
                                        │ 1:1
                                        │
                             ┌──────────▼──────────┐
                             │     TRANSACOES       │
                             ├─────────────────────┤
                             │ id (PK)             │
                             │ entrega_id (FK,UNQ) │
                             │ valor_bruto         │
                             │ taxa_plataforma     │
                             │ taxa_gateway        │
                             │ valor_liquido_loja  │
                             │ pagarme_txn_id      │
                             │ smart_pos_serial    │
                             │ status (ENUM)       │
                             │ liquidada           │
                             │ criado_em           │
                             │ processada_em       │
                             └─────────────────────┘
                                        │
                                        │ agrupadas em
                                        ▼
                             ┌─────────────────────┐
                             │ LIQUIDACOES_DIARIAS  │
                             ├─────────────────────┤
                             │ id (PK)             │
                             │ loja_id (FK)        │
                             │ data_referencia     │
                             │ total_entregas      │
                             │ valor_bruto         │
                             │ total_taxas         │
                             │ valor_liquido       │
                             │ pagarme_transfer_id │
                             │ status (ENUM)       │
                             │ processada_em       │
                             └─────────────────────┘
```

---

## Detalhes de cada tabela

### `usuarios`
Login unificado. `role` define permissões. `ref_id` vincula à entidade (loja ou motoboy).

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK, AUTO_INCREMENT | — |
| email | VARCHAR | UNIQUE, NOT NULL | Login |
| senha | VARCHAR | NOT NULL | Hash BCrypt |
| nome | VARCHAR | NOT NULL | — |
| role | VARCHAR (ENUM) | — | ADMIN, LOJA, MOTOBOY |
| ref_id | BIGINT | — | FK lógica para lojas ou motoboys |
| ativo | BOOLEAN | default true | — |
| criado_em | TIMESTAMP | — | — |

### `lojas`
Dados da loja + integração Pagar.me.

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK | — |
| nome | VARCHAR | NOT NULL | Nome fantasia |
| cnpj | VARCHAR | UNIQUE, NOT NULL | Documento |
| email | VARCHAR | UNIQUE, NOT NULL | — |
| telefone | VARCHAR | — | — |
| endereco | VARCHAR | — | Endereço de coleta |
| chave_pix | VARCHAR | — | Para liquidação |
| pagarme_recipient_id | VARCHAR | — | ID no Pagar.me |
| saldo_pendente | DECIMAL(10,2) | default 0 | Acumulado do dia |
| ativo | BOOLEAN | default true | — |
| criado_em | TIMESTAMP | — | — |

### `motoboys`
Dados do entregador + GPS + maquininha.

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK | — |
| nome | VARCHAR | NOT NULL | — |
| cpf | VARCHAR | UNIQUE, NOT NULL | — |
| email | VARCHAR | UNIQUE, NOT NULL | — |
| telefone | VARCHAR | — | — |
| cnh | VARCHAR | — | Opcional |
| smart_pos_serial | VARCHAR | UNIQUE | Serial da maquininha |
| fcm_token | VARCHAR | — | Token push Firebase |
| latitude_atual | DOUBLE | — | GPS tempo real |
| longitude_atual | DOUBLE | — | GPS tempo real |
| status | VARCHAR (ENUM) | — | DISPONIVEL, EM_ENTREGA, INATIVO |
| ativo | BOOLEAN | default true | — |
| criado_em | TIMESTAMP | — | — |

### `pedidos`
Pedido criado pela loja, com dados do cliente final.

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK | — |
| loja_id | BIGINT | FK → lojas, NOT NULL | Quem criou |
| cliente_nome | VARCHAR | — | Nome do consumidor |
| cliente_telefone | VARCHAR | — | — |
| endereco_entrega | VARCHAR | NOT NULL | Destino |
| latitude_entrega | DOUBLE | — | Coordenada GPS |
| longitude_entrega | DOUBLE | — | Coordenada GPS |
| valor_total | DECIMAL(10,2) | NOT NULL | Soma dos itens |
| taxa_entrega | DECIMAL(10,2) | default 0 | — |
| status | VARCHAR (ENUM) | — | Veja máquina de estados |
| observacao | TEXT | — | — |
| criado_em | TIMESTAMP | — | — |
| atualizado_em | TIMESTAMP | — | @PreUpdate |

### `itens_pedido`
Itens do pedido (1 pedido → N itens).

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK | — |
| pedido_id | BIGINT | FK → pedidos | — |
| descricao | VARCHAR | — | "Pizza Calabresa G" |
| quantidade | INT | — | — |
| valor_unitario | DECIMAL(10,2) | — | — |
| valor_total | DECIMAL(10,2) | — | qtd × unitário |

### `entregas`
Vincula pedido → motoboy. Controla o ciclo de entrega.

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK | — |
| pedido_id | BIGINT | FK → pedidos, UNIQUE | 1 entrega por pedido |
| motoboy_id | BIGINT | FK → motoboys | Pode ser null (disponível, sem motoboy) |
| status | VARCHAR (ENUM) | — | DISPONIVEL → ATRIBUIDA → COLETADA → FINALIZADA |
| version | BIGINT | — | Optimistic lock (evita dois motoboys aceitarem ao mesmo tempo) |
| atribuida_em | TIMESTAMP | — | Quando motoboy foi atribuído |
| coletada_em | TIMESTAMP | — | Quando coletou na loja |
| finalizada_em | TIMESTAMP | — | Quando entregou ao cliente |
| codigo_confirmacao | VARCHAR | — | Assinatura digital simples |
| criado_em | TIMESTAMP | — | — |

### `transacoes`
Registro de cada pagamento na maquininha.

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK | — |
| entrega_id | BIGINT | FK → entregas, UNIQUE | 1 transação por entrega |
| valor_bruto | DECIMAL(10,2) | NOT NULL | Total cobrado |
| taxa_plataforma | DECIMAL(10,2) | — | 5% |
| taxa_gateway | DECIMAL(10,2) | — | 2,5% |
| valor_liquido_loja | DECIMAL(10,2) | — | bruto - taxas |
| pagarme_transaction_id | VARCHAR | — | ID no Pagar.me |
| smart_pos_serial | VARCHAR | — | Qual maquininha processou |
| status | VARCHAR (ENUM) | — | PENDENTE, APROVADA, RECUSADA, ESTORNADA |
| liquidada | BOOLEAN | default false | Já incluída na liquidação? |
| criado_em | TIMESTAMP | — | — |
| processada_em | TIMESTAMP | — | — |

### `liquidacoes_diarias`
Resumo diário por loja — gerado automaticamente às 23:30.

| Coluna | Tipo | Constraint | Descrição |
|--------|------|-----------|-----------|
| id | BIGINT | PK | — |
| loja_id | BIGINT | FK → lojas, NOT NULL | — |
| data_referencia | DATE | NOT NULL | Dia da liquidação |
| total_entregas | INT | — | Qtd de entregas no dia |
| valor_bruto | DECIMAL(10,2) | — | Soma bruta |
| total_taxas | DECIMAL(10,2) | — | Soma de todas as taxas |
| valor_liquido | DECIMAL(10,2) | — | bruto - taxas |
| pagarme_transfer_id | VARCHAR | — | ID da transferência |
| status | VARCHAR (ENUM) | — | PENDENTE, PROCESSADA, FALHA |
| processada_em | TIMESTAMP | — | — |

---

## Relacionamentos

| De | Para | Tipo | FK |
|----|------|------|-----|
| Pedido | Loja | N:1 | `pedidos.loja_id → lojas.id` |
| ItemPedido | Pedido | N:1 | `itens_pedido.pedido_id → pedidos.id` |
| Entrega | Pedido | 1:1 | `entregas.pedido_id → pedidos.id` (UNIQUE) |
| Entrega | Motoboy | N:1 | `entregas.motoboy_id → motoboys.id` |
| Transacao | Entrega | 1:1 | `transacoes.entrega_id → entregas.id` (UNIQUE) |
| LiquidacaoDia | Loja | N:1 | `liquidacoes_diarias.loja_id → lojas.id` |
| Usuario | Loja/Motoboy | ref lógica | `usuarios.ref_id` (sem FK explícita) |

