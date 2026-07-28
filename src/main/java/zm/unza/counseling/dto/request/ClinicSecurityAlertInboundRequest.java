package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zm.unza.counseling.entity.SecurityAlert;

import java.time.LocalDateTime;

/**
 * Body shape posted by the clinic system to
 * POST /clinic/security-alerts/inbound
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicSecurityAlertInboundRequest {

    // Clinic's own local id for this alert
    private String externalAlertId;

    @NotNull(message = "Category is required")
    private SecurityAlert.Category category;

    @NotNull(message = "Severity is required")
    private SecurityAlert.Severity severity;

    @NotNull(message = "Source type is required")
    private SecurityAlert.SourceType sourceType;

    private String subjectStudentId;

    private String subjectName;

    private String reportedByName;

    private String description;

    private Double latitude;

    private Double longitude;

    private LocalDateTime occurredAt;
}
