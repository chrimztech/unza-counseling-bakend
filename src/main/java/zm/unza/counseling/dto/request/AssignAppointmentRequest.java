package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for assigning an appointment to a counselor
 */
@Data
public class AssignAppointmentRequest {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Counselor ID is required")
    private Long counselorId;

    private String notes; // Optional notes for the assignment
}
