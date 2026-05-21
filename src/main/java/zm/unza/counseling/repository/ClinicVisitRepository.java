package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.ClinicVisit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClinicVisitRepository extends JpaRepository<ClinicVisit, Long> {

    List<ClinicVisit> findByClientIdOrderByVisitDateDesc(Long clientId);

    Page<ClinicVisit> findByClientId(Long clientId, Pageable pageable);

    long countByClientId(Long clientId);

    @Query("SELECT COUNT(v) FROM ClinicVisit v WHERE v.client.id = :clientId AND v.visitDate >= :from AND v.visitDate <= :to")
    long countByClientIdAndDateRange(@Param("clientId") Long clientId,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    @Query("SELECT v FROM ClinicVisit v WHERE v.client.id = :clientId AND v.visitDate >= :from AND v.visitDate <= :to ORDER BY v.visitDate DESC")
    List<ClinicVisit> findByClientIdAndDateRange(@Param("clientId") Long clientId,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    // Clients with >= threshold visits in the given window (for frequent-visitor alerts)
    @Query(value = """
            SELECT v.client_id, COUNT(*) AS visit_count
            FROM clinic_visits v
            WHERE v.visit_date >= :from AND v.visit_date <= :to
            GROUP BY v.client_id
            HAVING COUNT(*) >= :threshold
            ORDER BY visit_count DESC
            """, nativeQuery = true)
    List<Object[]> findFrequentVisitors(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         @Param("threshold") long threshold);

    // Month-by-month breakdown for a client
    @Query(value = """
            SELECT DATE_TRUNC('month', visit_date) AS month, COUNT(*) AS cnt
            FROM clinic_visits
            WHERE client_id = :clientId
            GROUP BY DATE_TRUNC('month', visit_date)
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> countByClientIdGroupedByMonth(@Param("clientId") Long clientId);
}
