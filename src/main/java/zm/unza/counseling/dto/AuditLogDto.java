package zm.unza.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for audit log response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDto {
    
    private String id;
    private String action;
    private String entityType;
    private String entityId;
    private String userId;
    private String details;
    private String ipAddress;
    private String severity;
    private boolean success;
    private LocalDateTime createdAt;
}
