package zm.unza.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zm.unza.counseling.entity.Goal.GoalCategory;
import zm.unza.counseling.entity.Goal.GoalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for goal data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private String title;
    private String description;
    private GoalCategory category;
    private Integer targetValue;
    private Integer currentValue;
    private Integer progress;
    private GoalStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
