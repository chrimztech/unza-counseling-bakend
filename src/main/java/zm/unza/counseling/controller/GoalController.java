package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.GoalRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.GoalResponse;
import zm.unza.counseling.dto.response.GoalStatsResponse;
import zm.unza.counseling.entity.Goal.GoalCategory;
import zm.unza.counseling.entity.Goal.GoalStatus;
import zm.unza.counseling.service.GoalService;

import java.util.List;

/**
 * Controller for managing client wellness goals
 */
@RestController
@RequestMapping({"/api/v1/goals", "/api/goals", "/v1/goals", "/goals"})
@RequiredArgsConstructor
@Slf4j
public class GoalController {

    private final GoalService goalService;

    /**
     * Get all goals (Admin and Counselor only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<GoalResponse>>> getAllGoals(Pageable pageable) {
        log.info("Fetching all goals");
        return ResponseEntity.ok(ApiResponse.success(goalService.getAllGoals(pageable)));
    }

    /**
     * Get goal by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(@PathVariable Long id) {
        log.info("Fetching goal with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalById(id)));
    }

    /**
     * Get goals by client ID
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByClient(@PathVariable Long clientId) {
        log.info("Fetching goals for client: {}", clientId);
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalsByClient(clientId)));
    }

    /**
     * Get goals by client ID with pagination
     */
    @GetMapping("/client/{clientId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<GoalResponse>>> getGoalsByClientPaginated(
            @PathVariable Long clientId, Pageable pageable) {
        log.info("Fetching paginated goals for client: {}", clientId);
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalsByClient(clientId, pageable)));
    }

    /**
     * Get goals by client and status
     */
    @GetMapping("/client/{clientId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByClientAndStatus(
            @PathVariable Long clientId,
            @PathVariable GoalStatus status) {
        log.info("Fetching goals for client: {} with status: {}", clientId, status);
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalsByClientAndStatus(clientId, status)));
    }

    /**
     * Get goals by client and category
     */
    @GetMapping("/client/{clientId}/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByClientAndCategory(
            @PathVariable Long clientId,
            @PathVariable GoalCategory category) {
        log.info("Fetching goals for client: {} with category: {}", clientId, category);
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalsByClientAndCategory(clientId, category)));
    }

    /**
     * Create a new goal
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@Valid @RequestBody GoalRequest request) {
        log.info("Creating goal for client: {}", request.getClientId());
        return ResponseEntity.ok(ApiResponse.success(goalService.createGoal(request), "Goal created successfully"));
    }

    /**
     * Update a goal
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request) {
        log.info("Updating goal with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(goalService.updateGoal(id, request), "Goal updated successfully"));
    }

    /**
     * Delete a goal (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        log.info("Deleting goal with id: {}", id);
        goalService.deleteGoal(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully"));
    }

    /**
     * Update goal progress
     */
    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<GoalResponse>> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer currentValue) {
        log.info("Updating progress for goal: {} to value: {}", id, currentValue);
        return ResponseEntity.ok(ApiResponse.success(goalService.updateProgress(id, currentValue), "Progress updated successfully"));
    }

    /**
     * Update goal status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<GoalResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam GoalStatus status) {
        log.info("Updating status for goal: {} to: {}", id, status);
        return ResponseEntity.ok(ApiResponse.success(goalService.updateStatus(id, status), "Status updated successfully"));
    }

    /**
     * Get goal statistics for a client
     */
    @GetMapping("/client/{clientId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<GoalStatsResponse>> getGoalStats(@PathVariable Long clientId) {
        log.info("Fetching goal statistics for client: {}", clientId);
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalStats(clientId)));
    }

    /**
     * Get overdue goals
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getOverdueGoals() {
        log.info("Fetching overdue goals");
        return ResponseEntity.ok(ApiResponse.success(goalService.getOverdueGoals()));
    }

    /**
     * Search goals by keyword
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<GoalResponse>>> searchGoals(
            @RequestParam String keyword,
            Pageable pageable) {
        log.info("Searching goals with keyword: {}", keyword);
        return ResponseEntity.ok(ApiResponse.success(goalService.searchGoals(keyword, pageable)));
    }
}
