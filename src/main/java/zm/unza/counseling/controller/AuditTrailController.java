package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.AuditLogDto;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.AuditLog;
import zm.unza.counseling.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/audit", "/api/audit", "/v1/audit", "/audit"})
@RequiredArgsConstructor
public class AuditTrailController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Get all audit logs with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        List<AuditLog> logs = auditLogRepository.findAll();
        int start = page * size;
        int end = Math.min(start + size, logs.size());
        
        List<AuditLogDto> dtos = logs.subList(start, end).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Get audit logs by user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAuditLogsByUser(
            @PathVariable String userId) {
        
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<AuditLogDto> dtos = logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Get audit logs by date range
     */
    @GetMapping("/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<AuditLog> logs = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        List<AuditLogDto> dtos = logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Get failed/security audit logs
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getFailedLogs() {
        
        List<AuditLog> logs = auditLogRepository.findBySuccessFalseOrderByCreatedAtDesc();
        List<AuditLogDto> dtos = logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Search audit logs by action
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> searchAuditLogs(
            @RequestParam String action) {
        
        List<AuditLog> logs = auditLogRepository.findByActionContainingOrderByCreatedAtDesc(action);
        List<AuditLogDto> dtos = logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Get audit logs for specific entity
     */
    @GetMapping("/entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAuditLogsByEntity(
            @RequestParam String entityType,
            @RequestParam String entityId) {
        
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
        List<AuditLogDto> dtos = logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    private AuditLogDto convertToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId() != null ? log.getId().toString() : null)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .userId(log.getUserId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .severity(log.getSeverity())
                .success(log.isSuccess())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
