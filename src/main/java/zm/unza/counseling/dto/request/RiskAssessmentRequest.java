package zm.unza.counseling.dto.request;

import lombok.Data;
import zm.unza.counseling.entity.Client;

@Data
public class RiskAssessmentRequest {
    private Long clientId;
    private Integer riskScore;
    private Client.RiskLevel riskLevel;
    private String notes;
}