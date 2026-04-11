package zm.unza.counseling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zm.unza.counseling.entity.Session;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private Long id;
    private Long appointmentId;
    
    @JsonProperty("clientId")
    private Long studentId;
    
    private String studentName;
    private Long counselorId;
    private String counselorName;
    private LocalDateTime sessionDate;
    
    @JsonProperty("duration")
    private Integer durationMinutes;
    
    private String type;
    private String status;
    private String title;
    private String presentingIssue;
    private String outcome;

    public static SessionDto from(Session session) {
        return SessionDto.builder()
                .id(session.getId())
                .appointmentId(session.getAppointment() != null ? session.getAppointment().getId() : null)
                .studentId(session.getStudent().getId())
                .studentName(session.getStudent().getFullName())
                .counselorId(session.getCounselor().getId())
                .counselorName(session.getCounselor().getFullName())
                .sessionDate(session.getSessionDate())
                .durationMinutes(session.getDurationMinutes())
                .type(session.getType() != null ? session.getType().name() : null)
                .status(session.getStatus() != null ? session.getStatus().name() : null)
                .title(session.getTitle())
                .presentingIssue(session.getPresentingIssue())
                .outcome(session.getOutcome() != null ? session.getOutcome().name() : null)
                .build();
    }
    
    public Long getAppointmentId() { return appointmentId; }
    public Long getStudentId() { return studentId; }
    public Long getCounselorId() { return counselorId; }
    public LocalDateTime getSessionDate() { return sessionDate; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getTitle() { return title; }
    public String getPresentingIssue() { return presentingIssue; }
    public String getOutcome() { return outcome; }
}