package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.AcademicQualification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AcademicQualification entity
 */
@Repository
public interface AcademicQualificationRepository extends JpaRepository<AcademicQualification, Long> {

    /**
     * Find all qualifications for a client
     */
    List<AcademicQualification> findByClientIdOrderByAcademicYearDescSemesterDescCourseCodeAsc(Long clientId);

    /**
     * Find all qualifications for a student ID
     */
    List<AcademicQualification> findByStudentIdOrderByAcademicYearDescSemesterDesc(String studentId);

    /**
     * Find latest qualification for a client
     */
    Optional<AcademicQualification> findTopByClientIdOrderByCreatedAtDesc(Long clientId);

    /**
     * Find qualifications by course code
     */
    List<AcademicQualification> findByClientIdAndCourseCode(Long clientId, String courseCode);

    /**
     * Find qualifications by semester and academic year
     */
    List<AcademicQualification> findByClientIdAndSemesterAndAcademicYear(Long clientId, String semester, String academicYear);

    /**
     * Find failed courses for a client
     */
    List<AcademicQualification> findByClientIdAndCourseStatus(Long clientId, AcademicQualification.CourseStatus status);

    /**
     * Calculate average GPA for a client
     */
    @Query("SELECT AVG(a.gradePoint) FROM AcademicQualification a WHERE a.client.id = :clientId AND a.gradePoint IS NOT NULL")
    Optional<BigDecimal> calculateAverageGpaByClientId(@Param("clientId") Long clientId);

    /**
     * Calculate total credits earned
     */
    @Query("SELECT SUM(a.creditHours) FROM AcademicQualification a WHERE a.client.id = :clientId AND a.courseStatus = 'PASSED'")
    Optional<Integer> calculateTotalCreditsEarned(@Param("clientId") Long clientId);

    /**
     * Count failed courses
     */
    long countByClientIdAndCourseStatus(Long clientId, AcademicQualification.CourseStatus status);

    /**
     * Deactivate old qualifications before sync
     */
    @Modifying
    @Query("UPDATE AcademicQualification a SET a.isActive = false WHERE a.client.id = :clientId AND a.isActive = true")
    int deactivateQualificationsByClientId(@Param("clientId") Long clientId);

    /**
     * Find active qualifications for a client
     */
    List<AcademicQualification> findByClientIdAndIsActiveTrueOrderByAcademicYearDescSemesterDesc(Long clientId);

    /**
     * Get distinct academic years for a client
     */
    @Query("SELECT DISTINCT a.academicYear FROM AcademicQualification a WHERE a.client.id = :clientId ORDER BY a.academicYear DESC")
    List<String> findDistinctAcademicYearsByClientId(@Param("clientId") Long clientId);

    /**
     * Get distinct semesters for a client and academic year
     */
    @Query("SELECT DISTINCT a.semester FROM AcademicQualification a WHERE a.client.id = :clientId AND a.academicYear = :academicYear ORDER BY a.semester DESC")
    List<String> findDistinctSemestersByClientIdAndAcademicYear(@Param("clientId") Long clientId, @Param("academicYear") String academicYear);
}
