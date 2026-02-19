package zm.unza.counseling.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zm.unza.counseling.dto.request.GoalRequest;
import zm.unza.counseling.dto.response.GoalResponse;
import zm.unza.counseling.dto.response.GoalStatsResponse;
import zm.unza.counseling.entity.Goal.GoalCategory;
import zm.unza.counseling.entity.Goal.GoalStatus;

import java.util.List;

/**
 * Service interface for goal management
 */
public interface GoalService {

    /**
     * Get all goals with pagination
     * @param pageable pagination information
     * @return paginated list of goals
     */
    Page<GoalResponse> getAllGoals(Pageable pageable);

    /**
     * Get goal by ID
     * @param id the goal ID
     * @return the goal response
     */
    GoalResponse getGoalById(Long id);

    /**
     * Get all goals for a specific client
     * @param clientId the client ID
     * @return list of goals
     */
    List<GoalResponse> getGoalsByClient(Long clientId);

    /**
     * Get goals for a specific client with pagination
     * @param clientId the client ID
     * @param pageable pagination information
     * @return paginated list of goals
     */
    Page<GoalResponse> getGoalsByClient(Long clientId, Pageable pageable);

    /**
     * Get goals by client and status
     * @param clientId the client ID
     * @param status the goal status
     * @return list of goals
     */
    List<GoalResponse> getGoalsByClientAndStatus(Long clientId, GoalStatus status);

    /**
     * Get goals by client and category
     * @param clientId the client ID
     * @param category the goal category
     * @return list of goals
     */
    List<GoalResponse> getGoalsByClientAndCategory(Long clientId, GoalCategory category);

    /**
     * Create a new goal
     * @param request the goal request
     * @return the created goal
     */
    GoalResponse createGoal(GoalRequest request);

    /**
     * Update a goal
     * @param id the goal ID
     * @param request the goal request
     * @return the updated goal
     */
    GoalResponse updateGoal(Long id, GoalRequest request);

    /**
     * Delete a goal
     * @param id the goal ID
     */
    void deleteGoal(Long id);

    /**
     * Update goal progress
     * @param id the goal ID
     * @param currentValue the current value
     * @return the updated goal
     */
    GoalResponse updateProgress(Long id, Integer currentValue);

    /**
     * Update goal status
     * @param id the goal ID
     * @param status the new status
     * @return the updated goal
     */
    GoalResponse updateStatus(Long id, GoalStatus status);

    /**
     * Get goal statistics for a client
     * @param clientId the client ID
     * @return goal statistics
     */
    GoalStatsResponse getGoalStats(Long clientId);

    /**
     * Get overdue goals
     * @return list of overdue goals
     */
    List<GoalResponse> getOverdueGoals();

    /**
     * Search goals by keyword
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return paginated list of matching goals
     */
    Page<GoalResponse> searchGoals(String keyword, Pageable pageable);
}
