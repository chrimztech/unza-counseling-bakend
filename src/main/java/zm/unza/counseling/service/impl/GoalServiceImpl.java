package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.GoalRequest;
import zm.unza.counseling.dto.response.GoalResponse;
import zm.unza.counseling.dto.response.GoalStatsResponse;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Goal;
import zm.unza.counseling.entity.Goal.GoalCategory;
import zm.unza.counseling.entity.Goal.GoalStatus;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.GoalRepository;
import zm.unza.counseling.service.GoalService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of GoalService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final ClientRepository clientRepository;

    @Override
    public Page<GoalResponse> getAllGoals(Pageable pageable) {
        log.info("Fetching all goals");
        return goalRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public GoalResponse getGoalById(Long id) {
        log.info("Fetching goal with id: {}", id);
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        return mapToResponse(goal);
    }

    @Override
    public List<GoalResponse> getGoalsByClient(Long clientId) {
        log.info("Fetching goals for client: {}", clientId);
        return goalRepository.findByClientId(clientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<GoalResponse> getGoalsByClient(Long clientId, Pageable pageable) {
        log.info("Fetching goals for client: {} with pagination", clientId);
        return goalRepository.findByClientId(clientId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<GoalResponse> getGoalsByClientAndStatus(Long clientId, GoalStatus status) {
        log.info("Fetching goals for client: {} with status: {}", clientId, status);
        return goalRepository.findByClientIdAndStatus(clientId, status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalResponse> getGoalsByClientAndCategory(Long clientId, GoalCategory category) {
        log.info("Fetching goals for client: {} with category: {}", clientId, category);
        return goalRepository.findByClientIdAndCategory(clientId, category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        log.info("Creating goal for client: {}", request.getClientId());
        
        // Verify client exists
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));
        
        Goal goal = new Goal();
        goal.setClientId(request.getClientId());
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setCategory(GoalCategory.valueOf(request.getCategory()));
        goal.setTargetValue(request.getTargetValue());
        goal.setCurrentValue(request.getCurrentValue() != null ? request.getCurrentValue() : 0);
        goal.setProgress(request.getProgress() != null ? request.getProgress() : 0);
        goal.setStatus(request.getStatus() != null ? GoalStatus.valueOf(request.getStatus()) : GoalStatus.NOT_STARTED);
        goal.setDeadline(request.getDeadline());
        
        // Calculate progress if target and current values are set
        if (request.getTargetValue() != null && request.getTargetValue() > 0 && request.getCurrentValue() != null) {
            int progress = (int) ((request.getCurrentValue() * 100.0) / request.getTargetValue());
            goal.setProgress(Math.min(progress, 100));
        }
        
        Goal savedGoal = goalRepository.save(goal);
        log.info("Goal created successfully with id: {}", savedGoal.getId());
        
        return mapToResponse(savedGoal);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long id, GoalRequest request) {
        log.info("Updating goal with id: {}", id);
        
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            goal.setCategory(GoalCategory.valueOf(request.getCategory()));
        }
        goal.setTargetValue(request.getTargetValue());
        goal.setCurrentValue(request.getCurrentValue());
        goal.setDeadline(request.getDeadline());
        
        if (request.getStatus() != null) {
            goal.setStatus(GoalStatus.valueOf(request.getStatus()));
        }
        
        // Recalculate progress
        if (goal.getTargetValue() != null && goal.getTargetValue() > 0 && goal.getCurrentValue() != null) {
            int progress = (int) ((goal.getCurrentValue() * 100.0) / goal.getTargetValue());
            goal.setProgress(Math.min(progress, 100));
        }
        
        Goal updatedGoal = goalRepository.save(goal);
        log.info("Goal updated successfully with id: {}", updatedGoal.getId());
        
        return mapToResponse(updatedGoal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id) {
        log.info("Deleting goal with id: {}", id);
        
        if (!goalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Goal not found with id: " + id);
        }
        
        goalRepository.deleteById(id);
        log.info("Goal deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public GoalResponse updateProgress(Long id, Integer currentValue) {
        log.info("Updating progress for goal: {} to value: {}", id, currentValue);
        
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        
        goal.setCurrentValue(currentValue);
        
        // Calculate progress percentage
        if (goal.getTargetValue() != null && goal.getTargetValue() > 0) {
            int progress = (int) ((currentValue * 100.0) / goal.getTargetValue());
            goal.setProgress(Math.min(progress, 100));
            
            // Auto-update status if completed
            if (goal.getProgress() >= 100 && goal.getStatus() != GoalStatus.COMPLETED) {
                goal.setStatus(GoalStatus.COMPLETED);
                log.info("Goal {} automatically marked as completed", id);
            } else if (goal.getProgress() > 0 && goal.getStatus() == GoalStatus.NOT_STARTED) {
                goal.setStatus(GoalStatus.IN_PROGRESS);
                log.info("Goal {} automatically marked as in progress", id);
            }
        }
        
        Goal updatedGoal = goalRepository.save(goal);
        return mapToResponse(updatedGoal);
    }

    @Override
    @Transactional
    public GoalResponse updateStatus(Long id, GoalStatus status) {
        log.info("Updating status for goal: {} to: {}", id, status);
        
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        
        goal.setStatus(status);
        
        // If marking as completed, set progress to 100%
        if (status == GoalStatus.COMPLETED) {
            goal.setProgress(100);
            if (goal.getTargetValue() != null) {
                goal.setCurrentValue(goal.getTargetValue());
            }
        }
        
        Goal updatedGoal = goalRepository.save(goal);
        return mapToResponse(updatedGoal);
    }

    @Override
    public GoalStatsResponse getGoalStats(Long clientId) {
        log.info("Fetching goal statistics for client: {}", clientId);
        
        Object[] stats = goalRepository.getGoalStatsByClient(clientId);
        Double avgProgress = goalRepository.getAverageProgressByClient(clientId);
        List<Goal> overdueGoals = goalRepository.findOverdueGoals(LocalDate.now());
        
        long overdueCount = overdueGoals.stream()
                .filter(g -> g.getClientId().equals(clientId))
                .count();
        
        Object[] statArray = (Object[]) stats[0];
        
        return GoalStatsResponse.builder()
                .clientId(clientId)
                .totalGoals(((Number) statArray[0]).longValue())
                .completedGoals(((Number) statArray[1]).longValue())
                .inProgressGoals(((Number) statArray[2]).longValue())
                .notStartedGoals(((Number) statArray[3]).longValue())
                .abandonedGoals(((Number) statArray[4]).longValue())
                .averageProgress(avgProgress != null ? avgProgress : 0.0)
                .overdueGoals(overdueCount)
                .build();
    }

    @Override
    public List<GoalResponse> getOverdueGoals() {
        log.info("Fetching overdue goals");
        return goalRepository.findOverdueGoals(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<GoalResponse> searchGoals(String keyword, Pageable pageable) {
        log.info("Searching goals with keyword: {}", keyword);
        return goalRepository.searchGoals(keyword, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Map Goal entity to GoalResponse DTO
     */
    private GoalResponse mapToResponse(Goal goal) {
        String clientName = null;
        if (goal.getClient() != null) {
            clientName = goal.getClient().getFullName();
        }
        
        return GoalResponse.builder()
                .id(goal.getId())
                .clientId(goal.getClientId())
                .clientName(clientName)
                .title(goal.getTitle())
                .description(goal.getDescription())
                .category(goal.getCategory())
                .targetValue(goal.getTargetValue())
                .currentValue(goal.getCurrentValue())
                .progress(goal.getProgress())
                .status(goal.getStatus())
                .deadline(goal.getDeadline())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
