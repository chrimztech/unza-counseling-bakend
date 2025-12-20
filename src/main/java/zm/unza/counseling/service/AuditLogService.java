package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.AuditLog;
import zm.unza.counseling.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public void logAction(String action, String entityType, String entityId, String details, String userId, String ipAddress, boolean success) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setUserId(userId);
        log.setIpAddress(ipAddress);
        log.setSuccess(success);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public void logSecurityEvent(String action, String userId, String details, String ipAddress, boolean success, String severity) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUserId(userId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        log.setSuccess(success);
        log.setSeverity(severity);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> searchAuditLogs(String userId, String action, String entityType, LocalDateTime start, LocalDateTime end) {
        // Simplified implementation for compilation
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}