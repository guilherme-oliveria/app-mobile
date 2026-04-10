-- ===========================================================
-- V2 — Inserir usuário admin padrão
-- ===========================================================
-- Senha: admin123 (hash BCrypt)

INSERT INTO usuarios (email, senha, nome, role, ativo, criado_em)
VALUES (
    'admin@delivery.com',
    '$2a$10$N.zmdr9zkzoGtM7gKSomDOGHPibj.hAS5dMZRSBjbcFECNqDgr2Gy',
    'Administrador',
    'ADMIN',
    true,
    NOW()
)
ON CONFLICT (email) DO NOTHING;

