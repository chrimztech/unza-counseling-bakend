package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.unza.counseling.entity.CrisisAlert;

import java.util.List;

public interface CrisisAlertRepository extends JpaRepository<CrisisAlert, Long> {

    Page<CrisisAlert> findByStatusOrderByCreatedAtDesc(CrisisAlert.AlertStatus status, Pageable pageable);

    Page<CrisisAlert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(CrisisAlert.AlertStatus status);

    @Query("SELECT ca FROM CrisisAlert ca WHERE ca.client.id = :clientId ORDER BY ca.createdAt DESC")
    List<CrisisAlert> findByClientId(@Param("clientId") Long clientId);
}
