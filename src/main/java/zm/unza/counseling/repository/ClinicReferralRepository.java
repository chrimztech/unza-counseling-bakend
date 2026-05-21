package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.ClinicReferral;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicReferralRepository extends JpaRepository<ClinicReferral, Long> {

    Optional<ClinicReferral> findByReferralNumber(String referralNumber);

    List<ClinicReferral> findByClientIdOrderByCreatedAtDesc(Long clientId);

    Page<ClinicReferral> findByClientId(Long clientId, Pageable pageable);

    Page<ClinicReferral> findByCounselorId(Long counselorId, Pageable pageable);

    Page<ClinicReferral> findByLinkedCaseId(Long caseId, Pageable pageable);

    Page<ClinicReferral> findByStatus(ClinicReferral.ReferralStatus status, Pageable pageable);

    long countByClientId(Long clientId);

    long countByStatus(ClinicReferral.ReferralStatus status);

    @Query("SELECT r FROM ClinicReferral r ORDER BY r.createdAt DESC")
    Page<ClinicReferral> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM ClinicReferral r WHERE r.client.id = :clientId AND r.status NOT IN ('COMPLETED','DECLINED') ORDER BY r.createdAt DESC")
    List<ClinicReferral> findActiveByClientId(@Param("clientId") Long clientId);
}
