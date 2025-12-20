package zm.unza.counseling.dto;

import jakarta.validation.constraints.Future;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;

public class UpdateAppointmentRequest {
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;
    private Appointment.AppointmentStatus status;
    private String cancellationReason;

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public Appointment.AppointmentStatus getStatus() { return status; }
    public void setStatus(Appointment.AppointmentStatus status) { this.status = status; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
}