package zm.unza.counseling.dto;

import lombok.Data;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class UpdateAppointmentRequest {
    private String title;
    private String description;
    private Appointment.AppointmentStatus status;
    private LocalDateTime appointmentDate;
    private String cancellationReason;
    private Long caseId;
    private Map<String, Object> bookingDetails = new HashMap<>();
}
