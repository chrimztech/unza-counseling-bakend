package zm.unza.counseling.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.AcademicPerformanceDtos.*;
import zm.unza.counseling.dto.AcademicPerformanceSummary;
import zm.unza.counseling.dto.GpaTrendData;
import zm.unza.counseling.dto.StudentAtRiskDto;
import zm.unza.counseling.dto.AcademicStatistics;
import zm.unza.counseling.entity.AcademicPerformance;
import zm.unza.counseling.repository.AcademicPerformanceRepository;
import zm.unza.counseling.repository.ClientRepository;

import java.util.List;

@Service
@Transactional
public class AcademicPerformanceService {

    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public AcademicPerformanceService(AcademicPerformanceRepository academicPerformanceRepository, 
                                     ClientRepository clientRepository) {
        this.academicPerformanceRepository = academicPerformanceRepository;
        this.clientRepository = clientRepository;
    }

    public AcademicPerformanceResponse createAcademicPerformance(AcademicPerformanceRequest request) {
        // TODO: Implement this method
        return null;
    }

    public AcademicPerformanceResponse getById(Long id) {
        // TODO: Implement this method
        return null;
    }

    public List<AcademicPerformanceResponse> getByClientId(Long clientId) {
        // TODO: Implement this method
        return List.of();
    }

    public Page<AcademicPerformanceResponse> getByClientIdPaginated(Long clientId, Pageable pageable) {
        // TODO: Implement this method
        return Page.empty();
    }

    public AcademicPerformanceResponse getLatestForClient(Long clientId) {
        // TODO: Implement this method
        return null;
    }

    public AcademicPerformanceSummary getClientSummary(Long clientId) {
        // TODO: Implement this method
        return new AcademicPerformanceSummary();
    }

    public List<GpaTrendData> getGpaTrend(Long clientId) {
        // TODO: Implement this method
        return List.of();
    }

    public List<StudentAtRiskDto> getStudentsAtRisk() {
        // TODO: Implement this method
        return List.of();
    }

    public AcademicPerformanceResponse updateAcademicPerformance(Long id, AcademicPerformanceRequest request) {
        // TODO: Implement this method
        return null;
    }

    public void deleteAcademicPerformance(Long id) {
        // TODO: Implement this method
    }

    public AcademicStatistics getStatistics() {
        // TODO: Implement this method
        return new AcademicStatistics();
    }
}