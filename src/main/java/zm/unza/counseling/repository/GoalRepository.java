package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Goal;
import zm.unza.counseling.entity.Goal.GoalCategory;
import zm.unza.counseling.entity.Goal.GoalStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Goal entity
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Find all goals for a specific client
     * @param clientId the client ID
     * @return list of goals
     */
    List<Goal> findByClientId(Long clientId);

    /**
     * Find all goals for a specific client with pagination
     * @param clientId the client ID
     * @param pageable pagination information
     * @return paginated list of goals
     */
    Page<Goal> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find goals by client and status
     * @param clientId the client ID
     * @param status the goal status
     * @return list of goals
     */
    List<Goal> findByClientIdAndStatus(Long clientId, GoalStatus status);

    /**
     * Find goals by client and category
     * @param clientId the client ID
     * @param category the goal category
     * @return list of goals
     */
    List<Goal> findByClientIdAndCategory(Long clientId, GoalCategory category);

    /**
     * Find goals by status
     * @param status the goal status
     * @return list of goals
     */
    List<Goal> findByStatus(GoalStatus status);

    /**
     * Find goals by status with pagination
     * @param status the goal status
     * @param pageable pagination information
     * @return paginated list of goals
     */
    Page<Goal> findByStatus(GoalStatus status, Pageable pageable);

    /**
     * Find goals with deadline before a specific date
     * @param date the deadline date
     * @return list of goals
     */
    List<Goal> findByDeadlineBefore(LocalDate date);

    /**
     * Find goals with deadline between two dates
     * @param start start date
     * @param end end date
     * @return list of goals
     */
    List<Goal> findByDeadlineBetween(LocalDate start, LocalDate end);

    /**
     * Find overdue goals (deadline passed and not completed)
     * @param currentDate the current date
     * @return list of overdue goals
     */
    @Query("SELECT g FROM Goal g WHERE g.deadline < :currentDate AND g.status NOT IN ('COMPLETED', 'ABANDONED')")
    List<Goal> findOverdueGoals(@Param("currentDate") LocalDate currentDate);

    /**
     * Count goals by client and status
     * @param clientId the client ID
     * @param status the goal status
     * @return count of goals
     */
    Long countByClientIdAndStatus(Long clientId, GoalStatus status);

    /**
     * Count goals by client
     * @param clientId the client ID
     * @return count of goals
     */
    Long countByClientId(Long clientId);

    /**
     * Get goal statistics for a client
     * @param clientId the client ID
     * @return array with [total, completed, inProgress, notStarted, abandoned]
     */
    @Query("SELECT COUNT(g), " +
           "SUM(CASE WHEN g.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN g.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN g.status = 'NOT_STARTED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN g.status = 'ABANDONED' THEN 1 ELSE 0 END) " +
           "FROM Goal g WHERE g.clientId = :clientId")
    Object[] getGoalStatsByClient(@Param("clientId") Long clientId);

    /**
     * Get average progress for a client's goals
     * @param clientId the client ID
     * @return average progress
     */
    @Query("SELECT AVG(g.progress) FROM Goal g WHERE g.clientId = :clientId")
    Double getAverageProgressByClient(@Param("clientId") Long clientId);

    /**
     * Get average progress for a client's goals by category
     * @param clientId the client ID
     * @param category the goal category
     * @return average progress
     */
    @Query("SELECT AVG(g.progress) FROM Goal g WHERE g.clientId = :clientId AND g.category = :category")
    Double getAverageProgressByClientAndCategory(@Param("clientId") Long clientId, @Param("category") GoalCategory category);

    /**
     * Find goals by category
     * @param category the goal category
     * @param pageable pagination information
     * @return paginated list of goals
     */
    Page<Goal> findByCategory(GoalCategory category, Pageable pageable);

    /**
     * Search goals by title or description
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return paginated list of matching goals
     */
    @Query("SELECT g FROM Goal g WHERE " +
           "LOWER(g.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Goal> searchGoals(@Param("keyword") String keyword, Pageable pageable);
}
