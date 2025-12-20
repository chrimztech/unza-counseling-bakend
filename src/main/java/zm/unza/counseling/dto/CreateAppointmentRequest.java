package zm.unza.counseling.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;

public class CreateAppointmentRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotNull(message = "Student ID is required")
    private Long studentId;
    @NotNull(message = "Counselor ID is required")
    private Long counselorId;
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;
    @NotNull
    private Appointment.AppointmentType type;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCounselorId() { return counselorId; }
    public void setCounselorId(Long counselorId) { this.counselorId = counselorId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public Appointment.AppointmentType getType() { return type; }
    public void setType(Appointment.AppointmentType type) { this.type = type; }
}