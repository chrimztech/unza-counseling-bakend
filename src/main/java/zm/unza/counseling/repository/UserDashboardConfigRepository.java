package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.UserDashboardConfig;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDashboardConfigRepository extends JpaRepository<UserDashboardConfig, Long> {
    
    List<UserDashboardConfig> findByUserIdOrderByPositionYAscPositionXAsc(Long userId);
    
    Optional<UserDashboardConfig> findByUserIdAndWidgetId(Long userId, String widgetId);
    
    @Modifying
    @Query("DELETE FROM UserDashboardConfig c WHERE c.userId = :userId AND c.widgetId = :widgetId")
    void deleteByUserIdAndWidgetId(Long userId, String widgetId);

    @Modifying
    @Query("DELETE FROM UserDashboardConfig c WHERE c.userId = :userId")
    void deleteByUserId(Long userId);
    
    boolean existsByUserIdAndWidgetId(Long userId, String widgetId);
}
