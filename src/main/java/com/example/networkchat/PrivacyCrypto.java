package com.example.networkchat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public final class PrivacyCrypto {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_FACTORY = "PBKDF2WithHmacSHA256";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int ITERATIONS = 100_000;

    private PrivacyCrypto() {
    }

    public static String encrypt(String plainText, String passphrase) throws GeneralSecurityException {
        if (plainText == null) {
            return "";
        }
        String safePassphrase = normalizePassphrase(passphrase);
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        byte[] iv = new byte[IV_LENGTH_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);

        SecretKey secretKey = deriveKey(safePassphrase, salt);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] payload = new byte[salt.length + iv.length + cipherText.length];
        System.arraycopy(salt, 0, payload, 0, salt.length);
        System.arraycopy(iv, 0, payload, salt.length, iv.length);
        System.arraycopy(cipherText, 0, payload, salt.length + iv.length, cipherText.length);
        return Base64.getEncoder().encodeToString(payload);
    }

    public static String decrypt(String encodedText, String passphrase) throws GeneralSecurityException {
        if (encodedText == null || encodedText.trim().isEmpty()) {
            return "";
        }
        String safePassphrase = normalizePassphrase(passphrase);
        byte[] payload = Base64.getDecoder().decode(encodedText);
        if (payload.length <= SALT_LENGTH_BYTES + IV_LENGTH_BYTES) {
            throw new GeneralSecurityException("Invalid encrypted payload");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        byte[] iv = new byte[IV_LENGTH_BYTES];
        byte[] cipherText = new byte[payload.length - SALT_LENGTH_BYTES - IV_LENGTH_BYTES];

        System.arraycopy(payload, 0, salt, 0, SALT_LENGTH_BYTES);
        System.arraycopy(payload, SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES);
        System.arraycopy(payload, SALT_LENGTH_BYTES + IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

        SecretKey secretKey = deriveKey(safePassphrase, salt);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    public static String normalizePassphrase(String passphrase) {
        if (passphrase == null || passphrase.trim().isEmpty()) {
            return "privacy-default-passphrase";
        }
        return passphrase.trim();
    }

    private static SecretKey deriveKey(String passphrase, byte[] salt) throws GeneralSecurityException {
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
