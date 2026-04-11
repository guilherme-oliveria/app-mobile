package com.delivery.shared.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Scanner;

/**
 * Utilitário de criptografia AES-256-GCM com passphrase.
 * <p>
 * Usa uma frase secreta (texto) como chave para encriptar e descriptografar senhas.
 * <p>
 * Exemplo:
 *   Passphrase: "/caminho/arquivo/mobile"
 *   Encriptar:  CryptoUtil.encrypt("admin123", "/caminho/arquivo/mobile")
 *   Decriptar:  CryptoUtil.decrypt(textoCifrado, "/caminho/arquivo/mobile")
 */
public class CryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;  // bits
    private static final int IV_LENGTH = 12;         // bytes
    private static final int SALT_LENGTH = 16;       // bytes
    private static final int KEY_LENGTH = 256;        // bits (AES-256)
    private static final int ITERATIONS = 65536;      // PBKDF2

    // ═══════════════════════════════════════════════════════════
    //  MAIN — Menu interativo
    // ═══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);

        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║     🔐 Crypto Util — AES-256-GCM         ║");
        System.out.println("╠═══════════════════════════════════════════╣");
        System.out.println("║  1. Encriptar senha                      ║");
        System.out.println("║  2. Descriptografar senha                ║");
        System.out.println("║  0. Sair                                 ║");
        System.out.println("╚═══════════════════════════════════════════╝");

        while (true) {
            System.out.print("\nEscolha: ");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> menuEncrypt(scanner);
                case "2" -> menuDecrypt(scanner);
                case "0" -> { System.out.println("Saindo..."); return; }
                default  -> System.out.println("❌ Opção inválida.");
            }
        }
    }

    // ─── Menu: Encriptar ────────────────────────────────────────
    private static void menuEncrypt(Scanner scanner) {
        System.out.print("Digite a senha (texto plano): ");
        String senha = scanner.nextLine();
        System.out.print("Digite a passphrase (ex: /caminho/arquivo/mobile): ");
        String passphrase = scanner.nextLine();

        try {
            String encrypted = encrypt(senha, passphrase);
            System.out.println("\n✅ Senha encriptada com sucesso!");
            System.out.println("   Texto cifrado: " + encrypted);
            System.out.println("\n   👉 Guarde o texto cifrado e a passphrase.");
            System.out.println("   ⚠️  Sem a passphrase, NÃO é possível recuperar a senha!");
            System.out.println("\n   👉 Cole este valor na coluna 'senha' da migration SQL:");
            System.out.println("   '" + encrypted + "'");
        } catch (Exception e) {
            System.out.println("❌ Erro ao encriptar: " + e.getMessage());
        }
    }

    // ─── Menu: Descriptografar ──────────────────────────────────
    private static void menuDecrypt(Scanner scanner) {
        System.out.print("Cole o texto cifrado: ");
        String encrypted = scanner.nextLine().trim();
        System.out.print("Digite a passphrase: ");
        String passphrase = scanner.nextLine();

        try {
            String decrypted = decrypt(encrypted, passphrase);
            System.out.println("\n✅ Senha descriptografada!");
            System.out.println("   Senha original: " + decrypted);
        } catch (Exception e) {
            System.out.println("❌ Erro ao descriptografar: passphrase incorreta ou texto corrompido.");
        }
    }


    // ═══════════════════════════════════════════════════════════
    //  API Pública — usar em outras classes
    // ═══════════════════════════════════════════════════════════

    /**
     * Encripta uma senha usando AES-256-GCM com passphrase.
     *
     * @param textoPlano texto a encriptar (ex: "admin123")
     * @param passphrase frase secreta (ex: "/caminho/arquivo/mobile")
     * @return texto cifrado em Base64
     */
    public static String encrypt(String textoPlano, String passphrase) throws Exception {
        byte[] salt = randomBytes(SALT_LENGTH);
        byte[] iv   = randomBytes(IV_LENGTH);

        SecretKey key = deriveKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] cipherText = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

        // Formato final: salt (16) + iv (12) + cipherText (N)
        ByteBuffer buffer = ByteBuffer.allocate(SALT_LENGTH + IV_LENGTH + cipherText.length);
        buffer.put(salt);
        buffer.put(iv);
        buffer.put(cipherText);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Descriptografa um texto cifrado usando a passphrase.
     *
     * @param textoCifrado texto em Base64 gerado pelo encrypt()
     * @param passphrase   mesma frase secreta usada na encriptação
     * @return texto original (senha)
     */
    public static String decrypt(String textoCifrado, String passphrase) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(textoCifrado);
        ByteBuffer buffer = ByteBuffer.wrap(decoded);

        byte[] salt = new byte[SALT_LENGTH];
        buffer.get(salt);

        byte[] iv = new byte[IV_LENGTH];
        buffer.get(iv);

        byte[] cipherText = new byte[buffer.remaining()];
        buffer.get(cipherText);

        SecretKey key = deriveKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    // ═══════════════════════════════════════════════════════════
    //  Métodos internos
    // ═══════════════════════════════════════════════════════════

    /** Deriva chave AES-256 da passphrase usando PBKDF2 */
    private static SecretKey deriveKey(String passphrase, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /** Gera bytes aleatórios seguros */
    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}

