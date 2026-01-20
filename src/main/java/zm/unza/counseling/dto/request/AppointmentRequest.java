package zm.unza.counseling.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import zm.unza.counseling.entity.Appointment;

@Data
public class AppointmentRequest {
    private Long studentId;
    private Long counselorId;
    private String title;
    private LocalDateTime appointmentDate;
    private Appointment.AppointmentType type;
    private String notes;
}
