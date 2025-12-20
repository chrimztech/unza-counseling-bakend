package zm.unza.counseling.dto;

import lombok.Data;
import java.time.LocalDateTime;
import zm.unza.counseling.entity.Session;

@Data
public class SessionDto {
    private Long id;
    private Long appointmentId;
    private Long studentId;
    private String studentName;
    private Long counselorId;
    private String counselorName;
    private LocalDateTime sessionDate;
    private String title;
    private String sessionNotes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getCounselorId() { return counselorId; }
    public void setCounselorId(Long counselorId) { this.counselorId = counselorId; }
    public String getCounselorName() { return counselorName; }
    public void setCounselorName(String counselorName) { this.counselorName = counselorName; }
    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSessionNotes() { return sessionNotes; }
    public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }

    public static SessionDto from(Session session) {
        SessionDto dto = new SessionDto();
        dto.setId(session.getId());
        dto.setAppointmentId(session.getAppointment() != null ? session.getAppointment().getId() : null);
        dto.setStudentId(session.getStudent().getId());
        dto.setStudentName(session.getStudent().getFirstName() + " " + session.getStudent().getLastName());
        dto.setCounselorId(session.getCounselor().getId());
        dto.setCounselorName(session.getCounselor().getFirstName() + " " + session.getCounselor().getLastName());
        dto.setSessionDate(session.getSessionDate());
        dto.setTitle(session.getTitle());
        dto.setSessionNotes(session.getSessionNotes());
        return dto;
    }
}