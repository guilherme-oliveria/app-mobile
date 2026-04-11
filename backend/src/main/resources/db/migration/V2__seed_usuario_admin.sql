-- ===========================================================
-- V2 — Inserir usuário admin padrão
-- ===========================================================
-- Senha encriptada com AES-256-GCM (passphrase: crypto.passphrase)
-- Use CryptoUtil para descriptografar quando necessário

INSERT INTO usuarios (email, senha, nome, role, ativo, criado_em)
VALUES (
    'admin@delivery.com',
    '+pXQOCxZFUdrLr37A994izTKMfi6EqCft7aMBMCaF/EIvNax2ztNkVYmrCGtnbwSxAcPRKNHR1kf',
    'Administrador',
    'ADMIN',
    true,
    NOW()
)
ON CONFLICT (email) DO NOTHING;

