package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zm.unza.counseling.entity.SecurityAlert;

/**
 * Body shape posted by the clinic system to
 * PATCH /clinic/security-alerts/inbound/{id}/status
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicSecurityAlertStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private SecurityAlert.Status status;

    private String actorName;

    private String resolutionNotes;
}
