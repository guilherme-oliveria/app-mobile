package com.delivery.config;

import com.delivery.shared.util.CryptoUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder customizado usando AES-256-GCM.
 * <p>
 * No login, descriptografa o hash armazenado no banco e compara com a senha informada.
 * <p>
 * ⚠️ A passphrase é a chave mestra — se comprometida, todas as senhas ficam expostas.
 *    Configure via variável de ambiente ou application.properties:
 *    crypto.passphrase=/caminho/arquivo/mobile
 */
public class AesPasswordEncoder implements PasswordEncoder {

    private final String passphrase;

    public AesPasswordEncoder(String passphrase) {
        if (passphrase == null || passphrase.isBlank()) {
            throw new IllegalArgumentException("A passphrase não pode ser vazia. Configure 'crypto.passphrase' no application.properties");
        }
        this.passphrase = passphrase;
    }

    /**
     * Encripta a senha em texto plano usando AES-256-GCM.
     * Chamado ao cadastrar/alterar senha de um usuário.
     */
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            return CryptoUtil.encrypt(rawPassword.toString(), passphrase);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encriptar senha", e);
        }
    }

    /**
     * Verifica se a senha informada (login) bate com o valor encriptado no banco.
     * Descriptografa o valor do banco e compara com a senha em texto plano.
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            String decrypted = CryptoUtil.decrypt(encodedPassword, passphrase);
            return rawPassword.toString().equals(decrypted);
        } catch (Exception e) {
            // Passphrase errada ou dados corrompidos → senha não confere
            return false;
        }
    }
}

