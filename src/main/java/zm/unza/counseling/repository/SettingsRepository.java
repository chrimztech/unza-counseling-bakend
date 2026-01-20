package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Settings;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByKey(String key);
    List<Settings> findByCategory(Settings.SettingCategory category);
    
    @Query("SELECT s FROM Settings s WHERE s.category = :category AND s.active = true")
    List<Settings> findActiveByCategory(@Param("category") Settings.SettingCategory category);
    
    List<Settings> findByActiveTrue();
}