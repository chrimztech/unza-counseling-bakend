package zm.unza.counseling.dto;

import lombok.Data;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {
    private Long studentId;
    private Long counselorId;
    private String title;
    private String description;
    private LocalDateTime appointmentDate;
    private Appointment.AppointmentType type;
}