package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Scholarship;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

    List<Scholarship> findByStatus(Scholarship.ScholarshipStatus status);

    Page<Scholarship> findByStatus(Scholarship.ScholarshipStatus status, Pageable pageable);

    List<Scholarship> findByType(Scholarship.ScholarshipType type);

    @Query("SELECT s FROM Scholarship s WHERE s.deadline >= :today AND s.status = 'OPEN' ORDER BY s.deadline ASC")
    List<Scholarship> findActiveScholarships(@Param("today") LocalDate today);

    @Query("SELECT s FROM Scholarship s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.sponsor) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Scholarship> searchScholarships(@Param("query") String query, Pageable pageable);

    long countByStatus(Scholarship.ScholarshipStatus status);
}
