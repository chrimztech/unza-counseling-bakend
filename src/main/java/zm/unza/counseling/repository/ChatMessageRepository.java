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

    /** One conversation's unsummarised messages, oldest first (the verbatim context for that chat). */
    List<ChatMessage> findByUserIdAndSessionIdAndIdGreaterThanOrderByIdAsc(Long userId, String sessionId, Long afterId);

    /** Every AI chat exchange since the given time (admin oversight metrics). */
    List<ChatMessage> findByCreatedAtGreaterThanEqual(java.time.LocalDateTime since);

    @Transactional
    long deleteByUserIdAndSessionId(Long userId, String sessionId);
    @Transactional
    void deleteByUserId(Long userId);
}