package com.securevault.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vault/credentials")
@CrossOrigin(origins = "*")
public class CredentialController {

    private final CredentialService credentialService;

    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    // =========================
    // CREATE CREDENTIAL
    // =========================

    @PostMapping
    public ResponseEntity<?> createCredential(
            @RequestBody Credential credential,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            Credential savedCredential =
                    credentialService.createCredential(
                            credential,
                            email
                    );

            return ResponseEntity.ok(savedCredential);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // GET ALL USER CREDENTIALS
    // =========================

    @GetMapping
    public ResponseEntity<?> getCredentials(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            List<Credential> credentials =
                    credentialService.getUserCredentials(email);

            return ResponseEntity.ok(credentials);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // SEARCH USER CREDENTIALS
    // =========================

    @GetMapping("/search")
    public ResponseEntity<?> searchCredentials(
            @RequestParam String query,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            List<Credential> credentials =
                    credentialService.searchCredentials(
                            email,
                            query
                    );

            return ResponseEntity.ok(credentials);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // GET FAVORITE CREDENTIALS
    // =========================

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavoriteCredentials(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            List<Credential> credentials =
                    credentialService.getFavoriteCredentials(
                            email
                    );

            return ResponseEntity.ok(credentials);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // TOGGLE FAVORITE
    // =========================

    @PutMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            Credential credential =
                    credentialService.toggleFavorite(
                            id,
                            email
                    );

            return ResponseEntity.ok(credential);

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(e.getMessage());
        }
    }

    // =========================
    // GET SINGLE CREDENTIAL
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<?> getCredential(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            Credential credential =
                    credentialService.getCredentialById(
                            id,
                            email
                    );

            return ResponseEntity.ok(credential);

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(e.getMessage());
        }
    }

    // =========================
    // UPDATE CREDENTIAL
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCredential(
            @PathVariable Long id,
            @RequestBody Credential updatedCredential,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            Credential credential =
                    credentialService.updateCredential(
                            id,
                            updatedCredential,
                            email
                    );

            return ResponseEntity.ok(credential);

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(e.getMessage());
        }
    }

    // =========================
    // DELETE CREDENTIAL
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCredential(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            credentialService.deleteCredential(
                    id,
                    email
            );

            return ResponseEntity.ok(
                    "Credential deleted successfully"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(e.getMessage());
        }
    }
}