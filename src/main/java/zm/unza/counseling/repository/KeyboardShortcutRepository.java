package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.KeyboardShortcut;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeyboardShortcutRepository extends JpaRepository<KeyboardShortcut, Long> {
    
    List<KeyboardShortcut> findByUserIdAndEnabledTrue(Long userId);
    
    List<KeyboardShortcut> findByUserIdOrderByKeyAsc(Long userId);
    
    Optional<KeyboardShortcut> findByUserIdAndKey(Long userId, String key);

    void deleteByUserId(Long userId);
}
