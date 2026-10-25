package com.securevault.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginSecurityEventService loginSecurityEventService;
    private final MfaService mfaService;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            LoginSecurityEventService loginSecurityEventService,
            MfaService mfaService) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.loginSecurityEventService = loginSecurityEventService;
        this.mfaService = mfaService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(
            String name,
            String email,
            String password) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException(
                    "Email already registered");
        }

        String encryptedPassword =
                passwordEncoder.encode(password);

        User user = new User(
                name,
                email,
                encryptedPassword
        );

        return userRepository.save(user);
    }

    public String loginUser(
            String email,
            String password,
            String ipAddress,
            String device,
            String browser) {

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {

            loginSecurityEventService.recordLoginEvent(
                    null,
                    "FAILED",
                    ipAddress,
                    device,
                    browser
            );

            throw new RuntimeException(
                    "Invalid email or password");
        }

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            loginSecurityEventService.recordLoginEvent(
                    user,
                    "FAILED",
                    ipAddress,
                    device,
                    browser
            );

            throw new RuntimeException(
                    "Invalid email or password");
        }

        /*
         * If MFA is enabled, do not generate JWT yet.
         * The user must verify the MFA code first.
         */
        if (user.isMfaEnabled()) {

            return "MFA_REQUIRED";
        }

        loginSecurityEventService.recordLoginEvent(
                user,
                "SUCCESS",
                ipAddress,
                device,
                browser
        );

        return jwtService.generateToken(
                user.getEmail()
        );
    }

    /*
     * Generate JWT after successful MFA verification.
     */
    public String loginWithMfa(
            String email,
            String password,
            String mfaCode,
            String ipAddress,
            String device,
            String browser) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"));

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            loginSecurityEventService.recordLoginEvent(
                    user,
                    "FAILED",
                    ipAddress,
                    device,
                    browser
            );

            throw new RuntimeException(
                    "Invalid email or password");
        }

        if (!user.isMfaEnabled()) {

            throw new RuntimeException(
                    "MFA is not enabled for this account");
        }

        if (user.getMfaSecret() == null ||
                user.getMfaSecret().isBlank()) {

            throw new RuntimeException(
                    "MFA secret is not configured");
        }

        boolean validMfa =
                mfaService.verifyCode(
                        user.getMfaSecret(),
                        mfaCode
                );

        if (!validMfa) {

            loginSecurityEventService.recordLoginEvent(
                    user,
                    "FAILED",
                    ipAddress,
                    device,
                    browser
            );

            throw new RuntimeException(
                    "Invalid MFA code");
        }

        loginSecurityEventService.recordLoginEvent(
                user,
                "SUCCESS",
                ipAddress,
                device,
                browser
        );

        return jwtService.generateToken(
                user.getEmail()
        );
    }
}