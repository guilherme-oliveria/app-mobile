-- ===========================================================
-- V3 — Usuário padrão de Suporte
-- ===========================================================
-- Senha encriptada com AES-256-GCM (passphrase: crypto.passphrase)
-- Use CryptoUtil para descriptografar quando necessário

INSERT INTO usuarios (email, senha, nome, role, ativo, criado_em)
VALUES (
    'suporte@delivery.com',
    '/SJjeo4OdwPS7/pxJpXx4exV+cAyj11GFQMvHw/EVHqaLfcUMoVEWVvULmlvGn4V8EKtKT3ujsfx',
    'Suporte Operacional',
    'SUPORTE',
    true,
    NOW()
)
ON CONFLICT (email) DO NOTHING;

