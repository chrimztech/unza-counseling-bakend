package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.UserDashboardConfig;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDashboardConfigRepository extends JpaRepository<UserDashboardConfig, Long> {
    
    List<UserDashboardConfig> findByUserIdOrderByPositionYAscPositionXAsc(Long userId);
    
    Optional<UserDashboardConfig> findByUserIdAndWidgetId(Long userId, String widgetId);
    
    void deleteByUserIdAndWidgetId(Long userId, String widgetId);
    
    boolean existsByUserIdAndWidgetId(Long userId, String widgetId);
}
