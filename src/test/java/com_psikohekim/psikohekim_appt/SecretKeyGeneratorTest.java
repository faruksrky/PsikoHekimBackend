package com_psikohekim.psikohekim_appt;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class SecretKeyGeneratorTest {

    @Test
    public void generateOAuthStateSecretKey() {
        // UUID ile 32 karakterlik bir key oluştur
        String secretKey = UUID.randomUUID().toString().replace("-", "");

        // Oluşturulan key'i konsola yazdır
        System.out.println("\n=== OAuth State Secret Key ===");
        System.out.println("Generated key: " + secretKey);
        System.out.println("Key length: " + secretKey.length());
        System.out.println("\nAdd this to your application.yml:");
        System.out.println("oauth2:");
        System.out.println("  state:");
        System.out.println("    secret-key: " + secretKey);
        System.out.println("===============================\n");

        // Key uzunluğunu kontrol et
        assert secretKey.length() >= 32 : "Secret key should be at least 32 characters long";
    }
}