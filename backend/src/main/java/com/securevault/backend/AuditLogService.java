
package com.securevault.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logActivity(
            String userEmail,
            String action,
            String details) {

        AuditLog log = new AuditLog(
                userEmail,
                action,
                details,
                LocalDateTime.now()
        );

        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentActivity(String userEmail) {
        return auditLogRepository
                .findTop50ByUserEmailOrderByTimestampDesc(userEmail);
    }
}
