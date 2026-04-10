package zm.unza.counseling.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SavedSelfAssessmentDto {
    private Long id;
    private Long clientId;
    private LocalDateTime assessmentDate;
    private Integer phq9Score;
    private Integer gad7Score;
    private Integer pssScore;
    private Integer sleepQuality;
    private Integer overallWellness;
    private Boolean appetiteChanges;
    private Boolean concentrationDifficulty;
    private Boolean socialWithdrawal;
    private Boolean followUpRequired;
    private Boolean anonymous;
    private String title;
    private String description;
    private String assessmentType;
    private String recommendations;
    private LocalDateTime createdAt;
    private Map<String, Object> responses;
}
