package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request DTO for rescheduling an appointment
 */
@Data
public class RescheduleRequest {
    
    @NotNull(message = "Appointment date is required")
    private LocalDateTime appointmentDate;
    
    private String title;
}
