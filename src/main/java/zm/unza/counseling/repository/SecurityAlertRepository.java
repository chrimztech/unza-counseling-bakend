package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.unza.counseling.entity.SecurityAlert;

import java.util.List;

public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {

    List<SecurityAlert> findAllByOrderByCreatedAtDesc();

    @Query("SELECT s FROM SecurityAlert s " +
           "WHERE (:status IS NULL OR s.status = :status) " +
           "AND (:category IS NULL OR s.category = :category) " +
           "ORDER BY s.createdAt DESC")
    List<SecurityAlert> search(@Param("status") SecurityAlert.Status status,
                                @Param("category") SecurityAlert.Category category);
}
