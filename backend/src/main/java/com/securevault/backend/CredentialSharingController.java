package com.securevault.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sharing")
@CrossOrigin(origins = "*")
public class CredentialSharingController {

    private final CredentialService credentialService;

    public CredentialSharingController(
            CredentialService credentialService) {

        this.credentialService = credentialService;
    }

    // Share Credential
    @PostMapping("/share/{credentialId}")
    public ResponseEntity<?> shareCredential(
            @PathVariable Long credentialId,
            @RequestParam String sharedUserEmail,
            @RequestParam String permissionLevel,
            @RequestParam(required = false) String expiration,
            Authentication authentication) {

        try {

            String ownerEmail = authentication.getName();

            LocalDateTime expirationDate = null;

            if (expiration != null && !expiration.isBlank()) {
                expirationDate = LocalDateTime.parse(expiration);
            }

            Credential credential =
                    credentialService.shareCredential(
                            credentialId,
                            ownerEmail,
                            sharedUserEmail,
                            permissionLevel,
                            expirationDate
                    );

            return ResponseEntity.ok(credential);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get credentials shared with current user
    @GetMapping("/shared-with-me")
    public ResponseEntity<?> getSharedCredentials(
            Authentication authentication) {

        try {

            String email = authentication.getName();

            List<Credential> credentials =
                    credentialService.getSharedCredentials(email);

            return ResponseEntity.ok(credentials);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Remove Sharing
    @DeleteMapping("/remove/{credentialId}")
    public ResponseEntity<?> removeSharing(
            @PathVariable Long credentialId,
            Authentication authentication) {

        try {

            String ownerEmail = authentication.getName();

            Credential credential =
                    credentialService.removeSharing(
                            credentialId,
                            ownerEmail
                    );

            return ResponseEntity.ok(
                    "Credential sharing removed successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}