package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.ConsentForm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ConsentForm entities
 */
@Repository
public interface ConsentFormRepository extends JpaRepository<ConsentForm, Long> {

    /**
     * Find the latest active consent form
     */
    @Query("SELECT cf FROM ConsentForm cf WHERE cf.active = true ORDER BY cf.effectiveDate DESC, cf.createdAt DESC")
    List<ConsentForm> findLatestActive();

    /**
     * Find active consent forms by effective date
     */
    @Query("SELECT cf FROM ConsentForm cf WHERE cf.active = true AND cf.effectiveDate <= :date ORDER BY cf.effectiveDate DESC")
    List<ConsentForm> findActiveByEffectiveDate(@Param("date") LocalDateTime date);

    /**
     * Find consent form by version
     */
    Optional<ConsentForm> findByVersion(String version);

    /**
     * Find active consent forms
     */
    List<ConsentForm> findByActiveTrue();

    /**
     * Find consent forms by title
     */
    List<ConsentForm> findByTitleContainingIgnoreCase(String title);
}