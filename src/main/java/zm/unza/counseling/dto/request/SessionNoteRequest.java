package zm.unza.counseling.dto.request;

import lombok.Data;

@Data
public class SessionNoteRequest {
    private Long sessionId;
    private String content;
    private String privateNotes;
    private String nextSteps;
}
