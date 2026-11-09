package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ChatMessage> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    /** Messages not yet folded into the user's memory summary, oldest first. */
    List<ChatMessage> findByUserIdAndIdGreaterThanOrderByIdAsc(Long userId, Long afterId);
    @Transactional
    void deleteByUserId(Long userId);
}