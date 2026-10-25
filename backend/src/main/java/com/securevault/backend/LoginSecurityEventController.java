package com.securevault.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/login-history")
@CrossOrigin(origins = "*")
public class LoginSecurityEventController {

    private final LoginSecurityEventService loginSecurityEventService;
    private final UserRepository userRepository;

    public LoginSecurityEventController(
            LoginSecurityEventService loginSecurityEventService,
            UserRepository userRepository) {

        this.loginSecurityEventService = loginSecurityEventService;
        this.userRepository = userRepository;
    }

    // Get current user's login history
    @GetMapping
    public ResponseEntity<?> getLoginHistory(
            Authentication authentication) {

        try {

            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            List<LoginSecurityEvent> events =
                    loginSecurityEventService
                            .getUserLoginHistory(user);

            return ResponseEntity.ok(events);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}