package com.securevault.backend;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MfaService {

    private static final int SECRET_LENGTH = 20;
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;

    // Generate a new MFA secret
    public String generateSecret() {

        byte[] secret = new byte[SECRET_LENGTH];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secret);

        return Base64.getEncoder()
                .withoutPadding()
                .encodeToString(secret);
    }

    // Verify 6-digit MFA code
    public boolean verifyCode(
            String secret,
            String code) {

        if (secret == null || secret.isBlank()) {
            return false;
        }

        if (code == null ||
                !code.matches("\\d{" + CODE_DIGITS + "}")) {
            return false;
        }

        try {

            byte[] secretBytes =
                    Base64.getDecoder().decode(secret);

            long currentTime =
                    System.currentTimeMillis() / 1000;

            long counter =
                    currentTime / TIME_STEP_SECONDS;

            // Check current and nearby time windows
            for (long offset = -1; offset <= 1; offset++) {

                String generatedCode =
                        generateCode(
                                secretBytes,
                                counter + offset
                        );

                if (generatedCode.equals(code)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    private String generateCode(
            byte[] secret,
            long counter) throws Exception {

        byte[] counterBytes = new byte[8];

        for (int i = 7; i >= 0; i--) {

            counterBytes[i] =
                    (byte) (counter & 0xff);

            counter >>= 8;
        }

        Mac mac =
                Mac.getInstance("HmacSHA1");

        SecretKeySpec key =
                new SecretKeySpec(
                        secret,
                        "HmacSHA1"
                );

        mac.init(key);

        byte[] hash =
                mac.doFinal(counterBytes);

        int offset =
                hash[hash.length - 1] & 0x0f;

        int binary =
                ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);

        int otp =
                binary % 1_000_000;

        return String.format(
                "%06d",
                otp
        );
    }
}