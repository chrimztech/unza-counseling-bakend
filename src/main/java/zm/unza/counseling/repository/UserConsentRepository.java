package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.entity.UserConsent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserConsent entities
 */
@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    /**
     * Find user consent by user and consent form
     */
    Optional<UserConsent> findByUserAndConsentForm(User user, zm.unza.counseling.entity.ConsentForm consentForm);

    /**
     * Find user consents by user
     */
    List<UserConsent> findByUserOrderByConsentDateDesc(User user);

    /**
     * Find user consents by consent form
     */
    List<UserConsent> findByConsentFormOrderByConsentDateDesc(zm.unza.counseling.entity.ConsentForm consentForm);

    /**
     * Check if user has signed a specific consent form
     */
    @Query("SELECT COUNT(uc) > 0 FROM UserConsent uc WHERE uc.user = :user AND uc.consentForm = :consentForm")
    boolean existsByUserAndConsentForm(@Param("user") User user, @Param("consentForm") zm.unza.counseling.entity.ConsentForm consentForm);

    /**
     * Find latest consent for a user and consent form
     */
    @Query("SELECT uc FROM UserConsent uc WHERE uc.user = :user AND uc.consentForm = :consentForm ORDER BY uc.consentDate DESC")
    List<UserConsent> findLatestByUserAndConsentForm(@Param("user") User user, @Param("consentForm") zm.unza.counseling.entity.ConsentForm consentForm);

    /**
     * Find consents signed after a specific date
     */
    @Query("SELECT uc FROM UserConsent uc WHERE uc.user = :user AND uc.consentDate > :date ORDER BY uc.consentDate DESC")
    List<UserConsent> findByUserAndConsentDateAfter(@Param("user") User user, @Param("date") LocalDateTime date);

    /**
     * Count consents by consent form
     */
    @Query("SELECT COUNT(uc) FROM UserConsent uc WHERE uc.consentForm = :consentForm")
    long countByConsentForm(@Param("consentForm") zm.unza.counseling.entity.ConsentForm consentForm);

    /**
     * Count distinct users who have signed consent forms
     */
    @Query("SELECT COUNT(DISTINCT uc.user) FROM UserConsent uc")
    long countDistinctUsers();

    /**
     * Delete user consents by consent form
     */
    void deleteByConsentForm(zm.unza.counseling.entity.ConsentForm consentForm);
}