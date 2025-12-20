package zm.unza.counseling.service.interfaces;

import zm.unza.counseling.dto.request.AcademicPerformanceRequest;
import zm.unza.counseling.dto.response.AcademicPerformanceResponse;
import zm.unza.counseling.dto.AcademicPerformanceSummary;
import zm.unza.counseling.dto.AcademicStatistics;
import zm.unza.counseling.entity.AcademicPerformance;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for academic performance management
 * Provides abstraction layer for academic performance operations
 */
public interface AcademicPerformanceService {
    
    /**
     * Create a new academic performance record
     * 
     * @param request academic performance data
     * @return created academic performance response
     */
    AcademicPerformanceResponse createAcademicPerformance(AcademicPerformanceRequest request);
    
    /**
     * Update existing academic performance record
     * 
     * @param id academic performance ID
     * @param request updated data
     * @return updated academic performance response
     */
    AcademicPerformanceResponse updateAcademicPerformance(Long id, AcademicPerformanceRequest request);
    
    /**
     * Get academic performance by ID
     * 
     * @param id academic performance ID
     * @return academic performance response
     */
    Optional<AcademicPerformanceResponse> getAcademicPerformanceById(Long id);
    
    /**
     * Get all academic performance records with pagination
     * 
     * @param page page number
     * @param size page size
     * @return paginated list of academic performance records
     */
    List<AcademicPerformanceResponse> getAllAcademicPerformance(int page, int size);
    
    /**
     * Get academic performance by user ID
     * 
     * @param userId user ID
     * @return list of academic performance records
     */
    List<AcademicPerformanceResponse> getAcademicPerformanceByUserId(Long userId);
    
    /**
     * Get academic performance summary for a user
     * 
     * @param userId user ID
     * @return academic performance summary
     */
    AcademicPerformanceSummary getAcademicPerformanceSummary(Long userId);
    
    /**
     * Get academic statistics for analytics
     * 
     * @param startDate start date for statistics
     * @param endDate end date for statistics
     * @return academic statistics
     */
    AcademicStatistics getAcademicStatistics(String startDate, String endDate);
    
    /**
     * Delete academic performance record
     * 
     * @param id academic performance ID
     */
    void deleteAcademicPerformance(Long id);
    
    /**
     * Check if academic performance exists for user
     * 
     * @param userId user ID
     * @return true if exists
     */
    boolean existsByUserId(Long userId);
}