package zm.unza.counseling.dto.settings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for appointment settings
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentSettingsDTO {
    
    @NotNull(message = "Default session duration is required")
    @Min(value = 15, message = "Session duration must be at least 15 minutes")
    @Max(value = 120, message = "Session duration cannot exceed 120 minutes")
    private Integer defaultSessionDuration;
    
    @NotNull(message = "Max advance booking days is required")
    @Min(value = 1, message = "Max advance booking days must be at least 1")
    @Max(value = 90, message = "Max advance booking days cannot exceed 90")
    private Integer maxAdvanceBookingDays;
    
    @NotNull(message = "Cancellation deadline hours is required")
    @Min(value = 1, message = "Cancellation deadline must be at least 1 hour")
    @Max(value = 72, message = "Cancellation deadline cannot exceed 72 hours")
    private Integer cancellationDeadlineHours;
    
    @NotNull(message = "Auto confirm appointments setting is required")
    private Boolean autoConfirmAppointments;
}
