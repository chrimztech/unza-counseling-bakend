package zm.unza.counseling.repository;

import zm.unza.counseling.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Report entity
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Find reports by type
     * @param type the report type
     * @return list of reports
     */
    List<Report> findByType(String type);

    /**
     * Find reports by status
     * @param status the report status
     * @return list of reports
     */
    List<Report> findByStatus(String status);

    /**
     * Find reports by type and status
     * @param type the report type
     * @param status the report status
     * @return list of reports
     */
    List<Report> findByTypeAndStatus(String type, String status);

    /**
     * Count reports by type
     * @param type the report type
     * @return count of reports
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.type = :type")
    Long countByType(@Param("type") String type);

    /**
     * Count reports by status
     * @param status the report status
     * @return count of reports
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    Long countByStatus(@Param("status") String status);

    /**
     * Get all report types
     * @return list of report types
     */
    @Query("SELECT DISTINCT r.type FROM Report r")
    List<String> findAllTypes();
}