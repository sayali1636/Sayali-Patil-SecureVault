package com.securevault.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${encryption.key}")
    private String encryptionKey;

    private SecretKeySpec getKey() {
        return new SecretKeySpec(
                encryptionKey.getBytes(),
                "AES"
        );
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    getKey()
            );

            byte[] encryptedBytes =
                    cipher.doFinal(data.getBytes());

            return Base64.getEncoder()
                    .encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Encryption failed",
                    e
            );
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    getKey()
            );

            byte[] decryptedBytes =
                    cipher.doFinal(
                            Base64.getDecoder()
                                    .decode(encryptedData)
                    );

            return new String(decryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Decryption failed",
                    e
            );
        }
    }
}