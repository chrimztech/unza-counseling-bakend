package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.AcademicPerformance;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicPerformanceRepository extends JpaRepository<AcademicPerformance, Long> {
    List<AcademicPerformance> findByClientIdOrderByRecordDateDesc(Long clientId);
    Optional<AcademicPerformance> findTopByClientIdOrderByRecordDateDesc(Long clientId);
}