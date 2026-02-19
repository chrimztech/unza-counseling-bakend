package zm.unza.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for goal statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalStatsResponse {

    private Long clientId;
    private Long totalGoals;
    private Long completedGoals;
    private Long inProgressGoals;
    private Long notStartedGoals;
    private Long abandonedGoals;
    private Double averageProgress;
    private Long overdueGoals;
}
