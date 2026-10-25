package com.securevault.backend;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginSecurityEventService {

    private final LoginSecurityEventRepository eventRepository;

    public LoginSecurityEventService(
            LoginSecurityEventRepository eventRepository) {

        this.eventRepository = eventRepository;
    }

    // Record login activity
    public void recordLoginEvent(
            User user,
            String status,
            String ipAddress,
            String device,
            String browser) {

        LoginSecurityEvent event =
                new LoginSecurityEvent(
                        user,
                        LocalDateTime.now(),
                        status,
                        ipAddress,
                        device,
                        browser
                );

        eventRepository.save(event);
    }

    // Get user's login history
    public List<LoginSecurityEvent> getUserLoginHistory(
            User user) {

        return eventRepository
                .findByUserOrderByTimestampDesc(user);
    }
}