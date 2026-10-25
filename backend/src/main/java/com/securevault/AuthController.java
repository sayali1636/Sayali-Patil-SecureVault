package com.securevault.backend;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(
            AuthService authService,
            UserRepository userRepository) {

        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        try {

            User user = authService.registerUser(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword()
            );

            return ResponseEntity.ok(
                    "Registration successful"
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {

            String ipAddress =
                    httpRequest.getRemoteAddr();

            String userAgent =
                    httpRequest.getHeader("User-Agent");

            String device =
                    extractDevice(userAgent);

            String browser =
                    extractBrowser(userAgent);

            String result =
                    authService.loginUser(
                            request.getEmail(),
                            request.getPassword(),
                            ipAddress,
                            device,
                            browser
                    );

            /*
             * MFA is enabled.
             * JWT will be generated only after MFA verification.
             */
            if ("MFA_REQUIRED".equals(result)) {

                return ResponseEntity.ok(
                        new MfaRequiredResponse(
                                "MFA verification required",
                                true
                        )
                );
            }

            return ResponseEntity.ok(
                    new LoginResponse(
                            "Login successful",
                            result
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    /*
     * Verify MFA code and generate JWT.
     */
    @PostMapping("/login/mfa")
    public ResponseEntity<?> loginWithMfa(
            @RequestBody MfaLoginRequest request,
            HttpServletRequest httpRequest) {

        try {

            String ipAddress =
                    httpRequest.getRemoteAddr();

            String userAgent =
                    httpRequest.getHeader("User-Agent");

            String device =
                    extractDevice(userAgent);

            String browser =
                    extractBrowser(userAgent);

            String token =
                    authService.loginWithMfa(
                            request.getEmail(),
                            request.getPassword(),
                            request.getMfaCode(),
                            ipAddress,
                            device,
                            browser
                    );

            return ResponseEntity.ok(
                    new LoginResponse(
                            "Login successful",
                            token
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        return ResponseEntity.ok(
                new CurrentUserResponse(
                        "Authenticated user",
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                )
        );
    }

    private String extractDevice(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Mobile")) {
            return "Mobile";
        }

        if (userAgent.contains("Tablet")) {
            return "Tablet";
        }

        return "Desktop";
    }

    private String extractBrowser(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Edg")) {
            return "Microsoft Edge";
        }

        if (userAgent.contains("Chrome")) {
            return "Google Chrome";
        }

        if (userAgent.contains("Firefox")) {
            return "Mozilla Firefox";
        }

        if (userAgent.contains("Safari")) {
            return "Safari";
        }

        return "Unknown";
    }

    public static class RegisterRequest {

        private String name;
        private String email;
        private String password;

        public RegisterRequest() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginRequest {

        private String email;
        private String password;

        public LoginRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class MfaLoginRequest {

        private String email;
        private String password;
        private String mfaCode;

        public MfaLoginRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getMfaCode() {
            return mfaCode;
        }

        public void setMfaCode(String mfaCode) {
            this.mfaCode = mfaCode;
        }
    }

    public static class LoginResponse {

        private String message;
        private String token;

        public LoginResponse(
                String message,
                String token) {

            this.message = message;
            this.token = token;
        }

        public String getMessage() {
            return message;
        }

        public String getToken() {
            return token;
        }
    }

    public static class MfaRequiredResponse {

        private String message;
        private boolean mfaRequired;

        public MfaRequiredResponse(
                String message,
                boolean mfaRequired) {

            this.message = message;
            this.mfaRequired = mfaRequired;
        }

        public String getMessage() {
            return message;
        }

        public boolean isMfaRequired() {
            return mfaRequired;
        }
    }

    public static class CurrentUserResponse {

        private String message;
        private String name;
        private String email;
        private String role;

        public CurrentUserResponse(
                String message,
                String name,
                String email,
                String role) {

            this.message = message;
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public String getMessage() {
            return message;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }
}