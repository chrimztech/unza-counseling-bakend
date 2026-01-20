package zm.unza.counseling.dto;

import lombok.Data;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;

@Data
public class UpdateAppointmentRequest {
    private Appointment.AppointmentStatus status;
    private LocalDateTime appointmentDate;
    private String cancellationReason;
    private String title;
}