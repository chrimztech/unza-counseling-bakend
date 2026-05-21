package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zm.unza.counseling.entity.ClinicVisit;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicVisitRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long referralId;

    @NotNull(message = "Visit date is required")
    private LocalDateTime visitDate;

    private ClinicVisit.VisitType visitType = ClinicVisit.VisitType.GENERAL;

    private String visitPurpose;

    private String notes;

    // Set by the system; callers may override to 'CLINIC_WEBHOOK'
    private String recordedBy = "MANUAL";
}
