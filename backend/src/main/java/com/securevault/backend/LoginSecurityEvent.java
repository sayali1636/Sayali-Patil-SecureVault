package com.securevault.backend;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_security_events")
public class LoginSecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String status;

    private String ipAddress;

    private String device;

    private String browser;

    public LoginSecurityEvent() {
    }

    public LoginSecurityEvent(
            User user,
            LocalDateTime timestamp,
            String status,
            String ipAddress,
            String device,
            String browser) {

        this.user = user;
        this.timestamp = timestamp;
        this.status = status;
        this.ipAddress = ipAddress;
        this.device = device;
        this.browser = browser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }
}