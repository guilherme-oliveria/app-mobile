-- ===========================================================
-- V5 — Adiciona coluna atualizado_em nos cadastros
-- ===========================================================
ALTER TABLE usuarios  ADD COLUMN atualizado_em TIMESTAMP DEFAULT NOW();
ALTER TABLE lojas     ADD COLUMN atualizado_em TIMESTAMP DEFAULT NOW();
ALTER TABLE motoboys  ADD COLUMN atualizado_em TIMESTAMP DEFAULT NOW();

-- Preenche registros existentes com a data de criação
UPDATE usuarios  SET atualizado_em = criado_em WHERE atualizado_em IS NULL;
UPDATE lojas     SET atualizado_em = criado_em WHERE atualizado_em IS NULL;
UPDATE motoboys  SET atualizado_em = criado_em WHERE atualizado_em IS NULL;

