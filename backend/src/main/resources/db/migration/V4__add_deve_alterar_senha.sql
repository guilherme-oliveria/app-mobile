-- ===========================================================
-- V4 — Adiciona flag de troca de senha no primeiro login
-- ===========================================================
ALTER TABLE usuarios ADD COLUMN deve_alterar_senha BOOLEAN NOT NULL DEFAULT FALSE;

