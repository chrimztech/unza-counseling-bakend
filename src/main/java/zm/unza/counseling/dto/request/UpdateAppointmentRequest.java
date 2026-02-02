package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;

@Data
public class UpdateAppointmentRequest {
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;
    
    private Integer duration;
    
    private Appointment.AppointmentType type;
    
    private String description;
    
    private String meetingLink;
    
    private String location;
}
