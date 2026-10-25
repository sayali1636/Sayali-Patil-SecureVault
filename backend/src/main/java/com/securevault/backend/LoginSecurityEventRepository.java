package com.securevault.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginSecurityEventRepository
        extends JpaRepository<LoginSecurityEvent, Long> {

    // Get login activities of a particular user
    List<LoginSecurityEvent> findByUserOrderByTimestampDesc(User user);
}