package com.securevault.backend;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+";

    private final SecureRandom random = new SecureRandom();

    public String generatePassword(
            int length,
            boolean includeUppercase,
            boolean includeLowercase,
            boolean includeNumbers,
            boolean includeSpecial) {

        StringBuilder characters = new StringBuilder();

        if (includeUppercase) {
            characters.append(UPPERCASE);
        }

        if (includeLowercase) {
            characters.append(LOWERCASE);
        }

        if (includeNumbers) {
            characters.append(NUMBERS);
        }

        if (includeSpecial) {
            characters.append(SPECIAL);
        }

        if (characters.length() == 0) {
            throw new RuntimeException(
                    "At least one character type must be selected");
        }

        if (length < 4 || length > 128) {
            throw new RuntimeException(
                    "Password length must be between 4 and 128");
        }

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }
}