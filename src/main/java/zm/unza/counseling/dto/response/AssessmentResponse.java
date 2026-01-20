package zm.unza.counseling.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AssessmentResponse {
    private Long assessmentId;
    private String assessmentTitle;
    private Long clientId;
    private Map<String, Object> results;
    private LocalDateTime completedAt;
}
