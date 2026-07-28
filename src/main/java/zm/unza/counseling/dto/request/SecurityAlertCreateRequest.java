package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zm.unza.counseling.entity.SecurityAlert;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAlertCreateRequest {

    @NotNull(message = "Category is required")
    private SecurityAlert.Category category;

    @NotNull(message = "Severity is required")
    private SecurityAlert.Severity severity;

    private String subjectStudentId;

    private String subjectName;

    private String description;

    private Double latitude;

    private Double longitude;
}
