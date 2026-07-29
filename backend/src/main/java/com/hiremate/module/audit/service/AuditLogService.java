package com.hiremate.module.audit.service;

import com.hiremate.module.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void logEvent(Long userId, String userEmail, String action, String entityType, Long entityId, String ipAddress, String userAgent, String details);

    Page<AuditLog> getAllAuditLogs(Pageable pageable);
}
