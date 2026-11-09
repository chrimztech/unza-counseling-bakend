package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.AiProviderConfig;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiProviderConfigRepository extends JpaRepository<AiProviderConfig, Long> {

    List<AiProviderConfig> findAllByOrderByIsDefaultDescPriorityAscIdAsc();

    List<AiProviderConfig> findByEnabledTrueOrderByIsDefaultDescPriorityAscIdAsc();

    boolean existsByEnabledTrue();

    @Modifying
    @Transactional
    @Query("update AiProviderConfig c set c.isDefault = false where c.isDefault = true and c.id <> :keepId")
    void clearDefaultExcept(@Param("keepId") Long keepId);

    // Atomic counter updates so concurrent chats don't lose increments.

    @Modifying
    @Transactional
    @Query("update AiProviderConfig c set c.totalRequests = c.totalRequests + 1, c.lastUsedAt = :at where c.id = :id")
    void recordSuccess(@Param("id") Long id, @Param("at") LocalDateTime at);

    @Modifying
    @Transactional
    @Query("update AiProviderConfig c set c.totalRequests = c.totalRequests + 1, " +
           "c.failedRequests = c.failedRequests + 1, c.lastUsedAt = :at, c.lastError = :error where c.id = :id")
    void recordFailure(@Param("id") Long id, @Param("error") String error, @Param("at") LocalDateTime at);
}
