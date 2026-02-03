package zm.unza.counseling.dto;

import lombok.Data;
import zm.unza.counseling.entity.Appointment;
import java.time.LocalDateTime;

@Data
public class AppointmentDto {
    private Long id;
    private String title;
    private Long studentId;
    private String studentName;
    private Long counselorId;
    private String counselorName;
    private LocalDateTime appointmentDate;
    private Integer duration;
    private Appointment.AppointmentType type;
    private Appointment.AppointmentStatus status;
    private String description;
    private String meetingLink;
    private String location;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getCounselorId() { return counselorId; }
    public void setCounselorId(Long counselorId) { this.counselorId = counselorId; }
    public String getCounselorName() { return counselorName; }
    public void setCounselorName(String counselorName) { this.counselorName = counselorName; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Appointment.AppointmentType getType() { return type; }
    public void setType(Appointment.AppointmentType type) { this.type = type; }
    public Appointment.AppointmentStatus getStatus() { return status; }
    public void setStatus(Appointment.AppointmentStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static AppointmentDto from(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setTitle(appointment.getTitle());
        dto.setStudentId(appointment.getStudent().getId());
        dto.setStudentName(appointment.getStudent().getFirstName() + " " + appointment.getStudent().getLastName());
        
        // Handle null counselor (unassigned appointments)
        if (appointment.getCounselor() != null) {
            dto.setCounselorId(appointment.getCounselor().getId());
            dto.setCounselorName(appointment.getCounselor().getFirstName() + " " + appointment.getCounselor().getLastName());
        } else {
            dto.setCounselorId(null);
            dto.setCounselorName("Unassigned");
        }
        
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setDuration(appointment.getDuration());
        dto.setType(appointment.getType());
        dto.setStatus(appointment.getStatus());
        dto.setDescription(appointment.getDescription());
        dto.setMeetingLink(appointment.getMeetingLink());
        dto.setLocation(appointment.getLocation());
        dto.setCreatedAt(appointment.getCreatedAt());
        return dto;
    }
}
