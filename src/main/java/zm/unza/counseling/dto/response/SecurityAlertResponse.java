package zm.unza.counseling.dto.response;

import lombok.Data;
import zm.unza.counseling.entity.SecurityAlert;

import java.time.LocalDateTime;

@Data
public class SecurityAlertResponse {

    private Long id;
    private SecurityAlert.Category category;
    private SecurityAlert.Severity severity;
    private SecurityAlert.OriginSystem originSystem;
    private SecurityAlert.SourceType sourceType;
    private String subjectStudentId;
    private String subjectName;
    private String reportedByUserId;
    private String reportedByName;
    private String description;
    private Double latitude;
    private Double longitude;
    private SecurityAlert.Status status;
    private String acknowledgedByName;
    private LocalDateTime acknowledgedAt;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private String externalAlertId;
    private SecurityAlert.ExternalSystem externalSystem;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SecurityAlertResponse from(SecurityAlert a) {
        SecurityAlertResponse dto = new SecurityAlertResponse();
        dto.setId(a.getId());
        dto.setCategory(a.getCategory());
        dto.setSeverity(a.getSeverity());
        dto.setOriginSystem(a.getOriginSystem());
        dto.setSourceType(a.getSourceType());
        dto.setSubjectStudentId(a.getSubjectStudentId());
        dto.setSubjectName(a.getSubjectName());
        dto.setReportedByUserId(a.getReportedByUserId());
        dto.setReportedByName(a.getReportedByName());
        dto.setDescription(a.getDescription());
        dto.setLatitude(a.getLatitude());
        dto.setLongitude(a.getLongitude());
        dto.setStatus(a.getStatus());
        dto.setAcknowledgedByName(a.getAcknowledgedByName());
        dto.setAcknowledgedAt(a.getAcknowledgedAt());
        dto.setResolvedByName(a.getResolvedByName());
        dto.setResolvedAt(a.getResolvedAt());
        dto.setResolutionNotes(a.getResolutionNotes());
        dto.setExternalAlertId(a.getExternalAlertId());
        dto.setExternalSystem(a.getExternalSystem());
        dto.setOccurredAt(a.getOccurredAt());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
