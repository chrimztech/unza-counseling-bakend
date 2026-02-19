package zm.unza.counseling.dto;

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
    private LocalDateTime appointmentDate;
    
    private Integer duration = 60; // default 60 minutes
    
    @NotNull(message = "Appointment type is required")
    private String type;
    
    private String sessionMode = "IN_PERSON";
    
    private String meetingLink;
    
    private String location;
    
    public Appointment.AppointmentType getAppointmentType() {
        if (type == null) return null;
        try {
            return Appointment.AppointmentType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public Appointment.SessionMode getSessionModeEnum() {
        if (sessionMode == null) return Appointment.SessionMode.IN_PERSON;
        try {
            return Appointment.SessionMode.valueOf(sessionMode);
        } catch (IllegalArgumentException e) {
            return Appointment.SessionMode.IN_PERSON;
        }
    }
}