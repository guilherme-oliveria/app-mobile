-- ===========================================================
-- V1 — Criação de todas as tabelas do Delivery App
-- ===========================================================

-- ── Usuários (auth) ─────────────────────────────────────────
CREATE TABLE usuarios (
    id          BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    senha       VARCHAR(255)    NOT NULL,
    nome        VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL,
    ref_id      BIGINT,
    ativo       BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuarios_email ON usuarios (email);
CREATE INDEX idx_usuarios_role  ON usuarios (role);

-- ── Lojas ───────────────────────────────────────────────────
CREATE TABLE lojas (
    id                      BIGSERIAL       PRIMARY KEY,
    nome                    VARCHAR(255)    NOT NULL,
    cnpj                    VARCHAR(20)     NOT NULL UNIQUE,
    email                   VARCHAR(255)    NOT NULL UNIQUE,
    telefone                VARCHAR(20),
    endereco                VARCHAR(500),
    chave_pix               VARCHAR(255),
    pagarme_recipient_id    VARCHAR(100),
    saldo_pendente          NUMERIC(10,2)   NOT NULL DEFAULT 0.00,
    ativo                   BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em               TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lojas_cnpj  ON lojas (cnpj);
CREATE INDEX idx_lojas_email ON lojas (email);

-- ── Motoboys ────────────────────────────────────────────────
CREATE TABLE motoboys (
    id                  BIGSERIAL       PRIMARY KEY,
    nome                VARCHAR(255)    NOT NULL,
    cpf                 VARCHAR(14)     NOT NULL UNIQUE,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    telefone            VARCHAR(20),
    cnh                 VARCHAR(20),
    smart_pos_serial    VARCHAR(100)    UNIQUE,
    fcm_token           VARCHAR(500),
    latitude_atual      DOUBLE PRECISION,
    longitude_atual     DOUBLE PRECISION,
    status              VARCHAR(20)     NOT NULL DEFAULT 'DISPONIVEL',
    ativo               BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em           TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_motoboys_cpf    ON motoboys (cpf);
CREATE INDEX idx_motoboys_email  ON motoboys (email);
CREATE INDEX idx_motoboys_status ON motoboys (status);

-- ── Pedidos ─────────────────────────────────────────────────
CREATE TABLE pedidos (
    id                  BIGSERIAL       PRIMARY KEY,
    loja_id             BIGINT          NOT NULL REFERENCES lojas (id),
    cliente_nome        VARCHAR(255),
    cliente_telefone    VARCHAR(20),
    endereco_entrega    VARCHAR(500)    NOT NULL,
    latitude_entrega    DOUBLE PRECISION,
    longitude_entrega   DOUBLE PRECISION,
    valor_total         NUMERIC(10,2)   NOT NULL,
    taxa_entrega        NUMERIC(10,2)   NOT NULL DEFAULT 0.00,
    status              VARCHAR(30)     NOT NULL DEFAULT 'AGUARDANDO_ACEITE',
    observacao          TEXT,
    criado_em           TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em       TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pedidos_loja_id ON pedidos (loja_id);
CREATE INDEX idx_pedidos_status  ON pedidos (status);

-- ── Itens do Pedido ─────────────────────────────────────────
CREATE TABLE itens_pedido (
    id              BIGSERIAL       PRIMARY KEY,
    pedido_id       BIGINT          NOT NULL REFERENCES pedidos (id) ON DELETE CASCADE,
    descricao       VARCHAR(255)    NOT NULL,
    quantidade      INTEGER,
    valor_unitario  NUMERIC(10,2),
    valor_total     NUMERIC(10,2)
);

CREATE INDEX idx_itens_pedido_pedido_id ON itens_pedido (pedido_id);

-- ── Entregas ────────────────────────────────────────────────
CREATE TABLE entregas (
    id                  BIGSERIAL       PRIMARY KEY,
    pedido_id           BIGINT          NOT NULL UNIQUE REFERENCES pedidos (id),
    motoboy_id          BIGINT          REFERENCES motoboys (id),
    status              VARCHAR(20)     NOT NULL DEFAULT 'DISPONIVEL',
    version             BIGINT          NOT NULL DEFAULT 0,
    atribuida_em        TIMESTAMP,
    coletada_em         TIMESTAMP,
    finalizada_em       TIMESTAMP,
    codigo_confirmacao  VARCHAR(10),
    criado_em           TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_entregas_pedido_id  ON entregas (pedido_id);
CREATE INDEX idx_entregas_motoboy_id ON entregas (motoboy_id);
CREATE INDEX idx_entregas_status     ON entregas (status);

-- ── Transações (pagamento) ──────────────────────────────────
CREATE TABLE transacoes (
    id                      BIGSERIAL       PRIMARY KEY,
    entrega_id              BIGINT          NOT NULL UNIQUE REFERENCES entregas (id),
    valor_bruto             NUMERIC(10,2)   NOT NULL,
    taxa_plataforma         NUMERIC(10,2),
    taxa_gateway            NUMERIC(10,2),
    valor_liquido_loja      NUMERIC(10,2),
    pagarme_transaction_id  VARCHAR(100),
    smart_pos_serial        VARCHAR(100),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    liquidada               BOOLEAN         NOT NULL DEFAULT FALSE,
    criado_em               TIMESTAMP       NOT NULL DEFAULT NOW(),
    processada_em           TIMESTAMP
);

CREATE INDEX idx_transacoes_entrega_id ON transacoes (entrega_id);
CREATE INDEX idx_transacoes_status     ON transacoes (status);
CREATE INDEX idx_transacoes_liquidada  ON transacoes (liquidada);

-- ── Liquidações Diárias ─────────────────────────────────────
CREATE TABLE liquidacoes_diarias (
    id                  BIGSERIAL       PRIMARY KEY,
    loja_id             BIGINT          NOT NULL REFERENCES lojas (id),
    data_referencia     DATE            NOT NULL,
    total_entregas      INTEGER,
    valor_bruto         NUMERIC(10,2),
    total_taxas         NUMERIC(10,2),
    valor_liquido       NUMERIC(10,2),
    pagarme_transfer_id VARCHAR(100),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    processada_em       TIMESTAMP
);

CREATE INDEX idx_liquidacoes_loja_id ON liquidacoes_diarias (loja_id);
CREATE INDEX idx_liquidacoes_data    ON liquidacoes_diarias (data_referencia);

