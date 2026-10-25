package com.securevault.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/mfa")
@CrossOrigin(origins = "*")
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;

    public MfaController(
            MfaService mfaService,
            UserRepository userRepository) {

        this.mfaService = mfaService;
        this.userRepository = userRepository;
    }

    // =========================================
    // Generate MFA secret
    // =========================================
    @PostMapping("/setup")
    public ResponseEntity<?> setupMfa(
            Authentication authentication) {

        try {

            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            String secret = mfaService.generateSecret();

            user.setMfaSecret(secret);
            user.setMfaEnabled(false);

            userRepository.save(user);

            return ResponseEntity.ok(
                    new MfaSetupResponse(
                            "MFA setup secret generated",
                            secret
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================================
    // Verify MFA code and enable MFA
    // =========================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(
            @RequestBody MfaVerifyRequest request,
            Authentication authentication) {

        try {

            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            if (user.getMfaSecret() == null ||
                    user.getMfaSecret().isBlank()) {

                return ResponseEntity.badRequest()
                        .body("MFA setup is required first");
            }

            boolean valid =
                    mfaService.verifyCode(
                            user.getMfaSecret(),
                            request.getCode()
                    );

            if (!valid) {

                return ResponseEntity.badRequest()
                        .body("Invalid MFA code");
            }

            user.setMfaEnabled(true);

            userRepository.save(user);

            return ResponseEntity.ok(
                    "MFA enabled successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================================
    // Get MFA status
    // =========================================
    @GetMapping("/status")
    public ResponseEntity<?> getMfaStatus(
            Authentication authentication) {

        try {

            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            return ResponseEntity.ok(
                    new MfaStatusResponse(
                            user.isMfaEnabled()
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================================
    // MFA Verify Request
    // =========================================
    public static class MfaVerifyRequest {

        private String code;

        public MfaVerifyRequest() {
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    // =========================================
    // MFA Setup Response
    // =========================================
    public static class MfaSetupResponse {

        private String message;
        private String secret;

        public MfaSetupResponse(
                String message,
                String secret) {

            this.message = message;
            this.secret = secret;
        }

        public String getMessage() {
            return message;
        }

        public String getSecret() {
            return secret;
        }
    }

    // =========================================
    // MFA Status Response
    // =========================================
    public static class MfaStatusResponse {

        private boolean mfaEnabled;

        public MfaStatusResponse(
                boolean mfaEnabled) {

            this.mfaEnabled = mfaEnabled;
        }

        public boolean isMfaEnabled() {
            return mfaEnabled;
        }
    }
}