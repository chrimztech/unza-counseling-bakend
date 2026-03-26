package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.UserFeedback;

import java.util.List;

@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    
    List<UserFeedback> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<UserFeedback> findByStatusOrderByCreatedAtDesc(String status);
    
    List<UserFeedback> findByCategoryOrderByCreatedAtDesc(String category);
}
