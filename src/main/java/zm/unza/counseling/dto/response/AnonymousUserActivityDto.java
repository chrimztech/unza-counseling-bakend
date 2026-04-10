package zm.unza.counseling.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnonymousUserActivityDto {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastActivityAt;
    private long appointmentCount;
    private long messageCount;
    private long selfAssessmentCount;
    private long auditEventCount;
}
