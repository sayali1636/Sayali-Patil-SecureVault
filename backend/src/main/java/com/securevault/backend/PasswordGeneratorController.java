package com.securevault.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordGeneratorController {

    private final PasswordGeneratorService passwordGeneratorService;

    public PasswordGeneratorController(
            PasswordGeneratorService passwordGeneratorService) {

        this.passwordGeneratorService = passwordGeneratorService;
    }

    // Generate Password
    @GetMapping("/api/password/generate")
    public String generatePassword(

            @RequestParam(defaultValue = "12")
            int length,

            @RequestParam(defaultValue = "true")
            boolean uppercase,

            @RequestParam(defaultValue = "true")
            boolean lowercase,

            @RequestParam(defaultValue = "true")
            boolean numbers,

            @RequestParam(defaultValue = "true")
            boolean special) {

        return passwordGeneratorService.generatePassword(
                length,
                uppercase,
                lowercase,
                numbers,
                special
        );
    }

    // Password Strength Checker
    @GetMapping("/api/password/strength")
    public String checkPasswordStrength(
            @RequestParam String password) {

        int score = 0;

        if (password.length() >= 8) {
            score++;
        }

        if (password.matches(".*[A-Z].*")) {
            score++;
        }

        if (password.matches(".*[a-z].*")) {
            score++;
        }

        if (password.matches(".*[0-9].*")) {
            score++;
        }

        if (password.matches(".*[^a-zA-Z0-9].*")) {
            score++;
        }

        if (score <= 2) {
            return "Weak";
        } else if (score <= 4) {
            return "Medium";
        } else {
            return "Strong";
        }
    }
}