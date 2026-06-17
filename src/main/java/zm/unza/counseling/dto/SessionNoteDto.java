package zm.unza.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.SessionNote;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionNoteDto {
    private Long id;
    private Long sessionId;
    private String content;
    private String privateNotes;
    private String nextSteps;
    private String clientName;
    private String counselorName;
    private LocalDateTime sessionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SessionNoteDto from(SessionNote note) {
        Session session = note.getSession();
        return SessionNoteDto.builder()
                .id(note.getId())
                .sessionId(session != null ? session.getId() : null)
                .content(note.getContent())
                .privateNotes(note.getPrivateNotes())
                .nextSteps(note.getNextSteps())
                .clientName(session != null && session.getStudent() != null ? session.getStudent().getFullName() : null)
                .counselorName(session != null && session.getCounselor() != null ? session.getCounselor().getFullName() : null)
                .sessionDate(session != null ? session.getSessionDate() : null)
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
