package zm.unza.counseling.dto;

import lombok.Data;
import zm.unza.counseling.entity.Appointment;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AppointmentDto {
    private Long id;
    private String title;
    private Long studentId;
    private String studentName;
    private Long clientId;
    private String clientName;
    private Long counselorId;
    private String counselorName;
    private Long caseId;
    private String caseNumber;
    private Long sessionId;
    private LocalDateTime appointmentDate;
    private Integer duration;
    private Appointment.AppointmentType type;
    private Appointment.AppointmentStatus status;
    private Appointment.SessionMode sessionMode;
    private String description;
    private String meetingLink;
    private String meetingProvider;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> bookingDetails;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public Long getCounselorId() { return counselorId; }
    public void setCounselorId(Long counselorId) { this.counselorId = counselorId; }
    public String getCounselorName() { return counselorName; }
    public void setCounselorName(String counselorName) { this.counselorName = counselorName; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Appointment.AppointmentType getType() { return type; }
    public void setType(Appointment.AppointmentType type) { this.type = type; }
    public Appointment.AppointmentStatus getStatus() { return status; }
    public void setStatus(Appointment.AppointmentStatus status) { this.status = status; }
    public Appointment.SessionMode getSessionMode() { return sessionMode; }
    public void setSessionMode(Appointment.SessionMode sessionMode) { this.sessionMode = sessionMode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public String getMeetingProvider() { return meetingProvider; }
    public void setMeetingProvider(String meetingProvider) { this.meetingProvider = meetingProvider; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Map<String, Object> getBookingDetails() { return bookingDetails; }
    public void setBookingDetails(Map<String, Object> bookingDetails) { this.bookingDetails = bookingDetails; }

    public static AppointmentDto from(Appointment appointment) {
        return from(appointment, null);
    }

    public static AppointmentDto from(Appointment appointment, Map<String, Object> bookingDetails) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setTitle(appointment.getTitle());
        dto.setStudentId(appointment.getStudent().getId());
        dto.setStudentName(appointment.getStudent().getFirstName() + " " + appointment.getStudent().getLastName());
        if (appointment.getClient() != null) {
            dto.setClientId(appointment.getClient().getId());
            dto.setClientName(appointment.getClient().getFullName());
        }
        
        // Handle null counselor (unassigned appointments)
        if (appointment.getCounselor() != null) {
            dto.setCounselorId(appointment.getCounselor().getId());
            dto.setCounselorName(appointment.getCounselor().getFirstName() + " " + appointment.getCounselor().getLastName());
        } else {
            dto.setCounselorId(null);
            dto.setCounselorName("Unassigned");
        }

        if (appointment.getCaseEntity() != null) {
            dto.setCaseId(appointment.getCaseEntity().getId());
            dto.setCaseNumber(appointment.getCaseEntity().getCaseNumber());
        }
        
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setDuration(appointment.getDuration());
        dto.setType(appointment.getType());
        dto.setStatus(appointment.getStatus());
        dto.setSessionMode(appointment.getSessionMode());
        dto.setDescription(appointment.getDescription());
        dto.setMeetingLink(appointment.getMeetingLink());
        dto.setMeetingProvider(appointment.getMeetingProvider());
        dto.setLocation(appointment.getLocation());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        dto.setBookingDetails(bookingDetails);
        return dto;
    }
}
