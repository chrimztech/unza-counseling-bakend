package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zm.unza.counseling.entity.ClinicReferral;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicReferralRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long caseId;

    @NotBlank(message = "Referral reason is required")
    private String reason;

    private String clinicalNotes;

    private ClinicReferral.Urgency urgency = ClinicReferral.Urgency.ROUTINE;

    private LocalDateTime clinicAppointmentDate;
}
