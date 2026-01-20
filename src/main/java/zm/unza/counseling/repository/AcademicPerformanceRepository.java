package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.AcademicPerformance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicPerformanceRepository extends JpaRepository<AcademicPerformance, Long> {
    List<AcademicPerformance> findByClientIdOrderByRecordDateDesc(Long clientId);
    Optional<AcademicPerformance> findTopByClientIdOrderByRecordDateDesc(Long clientId);
    List<AcademicPerformance> findByGpaLessThan(BigDecimal threshold);
    List<AcademicPerformance> findByFaculty(String faculty);
    
    @org.springframework.data.jpa.repository.Query("SELECT AVG(a.gpa) FROM AcademicPerformance a")
    Double getAverageGpa();
    
    long count();
}