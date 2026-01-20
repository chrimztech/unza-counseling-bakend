package zm.unza.counseling.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class AssessmentSubmissionRequest {
    private Long assessmentId;
    private Long clientId;
    private Map<String, Object> answers;
}
