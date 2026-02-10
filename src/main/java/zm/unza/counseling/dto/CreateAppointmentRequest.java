package zm.unza.counseling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {
    // studentId is now obtained from the authenticated user
    private String studentId;
    
    private Long counselorId;
    
    @NotNull(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Appointment date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentDate;
    
    private Integer duration = 60; // default 60 minutes
    
    @NotNull(message = "Appointment type is required")
    private String type;
    
    public Appointment.AppointmentType getAppointmentType() {
        if (type == null) return null;
        try {
            return Appointment.AppointmentType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}