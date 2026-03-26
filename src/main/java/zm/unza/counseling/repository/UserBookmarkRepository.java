package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.UserBookmark;

import java.util.List;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {
    
    List<UserBookmark> findByUserIdOrderByLastUsedAtDesc(Long userId);
    
    List<UserBookmark> findByUserIdAndCategoryOrderByUsageCountDesc(Long userId, String category);
    
    List<UserBookmark> findByUserIdOrderByUsageCountDesc(Long userId);
}
