package zm.unza.counseling.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.GpaTrendData;
import zm.unza.counseling.dto.AcademicPerformanceSummary;
import zm.unza.counseling.dto.AcademicStatistics;
import zm.unza.counseling.dto.StudentAtRiskDto;
import zm.unza.counseling.dto.request.AcademicPerformanceRequest;
import zm.unza.counseling.dto.response.AcademicPerformanceResponse;
import zm.unza.counseling.entity.AcademicPerformance;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.AcademicPerformanceRepository;
import zm.unza.counseling.repository.ClientRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        AcademicPerformance entity = new AcademicPerformance();
        entity.setClient(client);
        mapRequestToEntity(request, entity);

        AcademicPerformance saved = academicPerformanceRepository.save(entity);
        return mapEntityToResponse(saved);
    }

    public AcademicPerformanceResponse getById(Long id) {
        AcademicPerformance entity = academicPerformanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic performance record not found"));
        return mapEntityToResponse(entity);
    }

    public List<AcademicPerformanceResponse> getByClientId(Long clientId) {
        return academicPerformanceRepository.findByClientIdOrderByRecordDateDesc(clientId)
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public Page<AcademicPerformanceResponse> getByClientIdPaginated(Long clientId, Pageable pageable) {
        // Note: Repository method for pagination needs to be added if strictly required, 
        // but for now we can filter the list or add the method to repo. 
        // Assuming list size is manageable or repo updated.
        // For strict pagination, we'd add findByClientId(Long, Pageable) to repo.
        // Here we'll just return empty for safety or implement if repo updated.
        // Let's assume we want to implement it properly:
        // Since repo interface in context didn't have Pageable method, we'll skip or assume it exists.
        // Given the context, let's return a page from the list for now.
        List<AcademicPerformanceResponse> all = getByClientId(clientId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        if (start > all.size()) return Page.empty();
        return new org.springframework.data.domain.PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    public AcademicPerformanceResponse getLatestForClient(Long clientId) {
        return academicPerformanceRepository.findTopByClientIdOrderByRecordDateDesc(clientId)
                .map(this::mapEntityToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No academic records found for client"));
    }

    public AcademicPerformanceSummary getClientSummary(Long clientId) {
        List<AcademicPerformance> records = academicPerformanceRepository.findByClientIdOrderByRecordDateDesc(clientId);
        
        if (records.isEmpty()) {
            return AcademicPerformanceSummary.builder()
                    .clientId(clientId)
                    .currentGpa(BigDecimal.ZERO)
                    .averageGpa(BigDecimal.ZERO)
                    .build();
        }

        AcademicPerformance latest = records.get(0);
        double avgGpa = records.stream()
                .map(r -> r.getGpa().doubleValue())
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);

        return AcademicPerformanceSummary.builder()
                .clientId(clientId)
                .clientName(latest.getClient().getFirstName() + " " + latest.getClient().getLastName())
                .currentGpa(latest.getGpa())
                .averageGpa(BigDecimal.valueOf(avgGpa))
                .currentStanding(latest.getAcademicStanding())
                .lastRecordDate(latest.getRecordDate())
                .trend(calculateTrend(records))
                .build();
    }

    public List<GpaTrendData> getGpaTrend(Long clientId) {
        return academicPerformanceRepository.findByClientIdOrderByRecordDateDesc(clientId).stream()
                .map(r -> GpaTrendData.builder()
                        .date(r.getRecordDate())
                        .gpa(r.getGpa())
                        .academicYear(r.getAcademicYear())
                        .semester(r.getSemester())
                        .build())
                .collect(Collectors.toList());
    }

    public List<StudentAtRiskDto> getStudentsAtRisk() {
        // Logic to identify at-risk students (e.g., GPA < 2.0)
        return academicPerformanceRepository.findByGpaLessThan(new BigDecimal("2.0")).stream()
                .map(r -> StudentAtRiskDto.builder()
                        .clientId(r.getClient().getId())
                        .riskLevel("HIGH") // Simplified logic
                        .build())
                .distinct()
                .collect(Collectors.toList());
    }

    public AcademicPerformanceResponse updateAcademicPerformance(Long id, AcademicPerformanceRequest request) {
        AcademicPerformance entity = academicPerformanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        
        mapRequestToEntity(request, entity);
        return mapEntityToResponse(academicPerformanceRepository.save(entity));
    }

    public void deleteAcademicPerformance(Long id) {
        academicPerformanceRepository.deleteById(id);
    }

    public AcademicStatistics getStatistics() {
        Double avg = academicPerformanceRepository.getAverageGpa();
        long count = academicPerformanceRepository.count();
        
        return AcademicStatistics.builder()
                .averageGpa(avg != null ? BigDecimal.valueOf(avg) : BigDecimal.ZERO)
                .totalStudents(count)
                .build();
    }

    public List<AcademicPerformanceResponse> getStudentsWithLowGpa(java.math.BigDecimal threshold) {
        return academicPerformanceRepository.findByGpaLessThan(threshold).stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<AcademicPerformanceResponse> getByFaculty(String faculty) {
        return academicPerformanceRepository.findByFaculty(faculty).stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(AcademicPerformanceRequest req, AcademicPerformance entity) {
        entity.setAcademicYear(req.getAcademicYear());
        entity.setSemester(req.getSemester());
        entity.setGpa(req.getGpa());
        entity.setTotalCredits(req.getTotalCredits());
        entity.setCreditsCompleted(req.getCreditsCompleted());
        entity.setCreditsFailed(req.getCreditsFailed());
        entity.setAttendanceRate(req.getAttendanceRate());
        entity.setAssignmentsCompleted(req.getAssignmentsCompleted());
        entity.setAssignmentsTotal(req.getAssignmentsTotal());
        entity.setAcademicStanding(req.getAcademicStanding());
        entity.setCoursesDropped(req.getCoursesDropped());
        entity.setCoursesWithdrawn(req.getCoursesWithdrawn());
        entity.setStudyProgram(req.getStudyProgram());
        entity.setYearOfStudy(req.getYearOfStudy());
        entity.setFaculty(req.getFaculty());
        entity.setDepartment(req.getDepartment());
        entity.setRecordDate(req.getRecordDate());
        entity.setNotes(req.getNotes());
    }

    private AcademicPerformanceResponse mapEntityToResponse(AcademicPerformance entity) {
        return AcademicPerformanceResponse.builder()
                .id(entity.getId())
                .clientId(entity.getClient().getId())
                .clientName(entity.getClient().getFirstName() + " " + entity.getClient().getLastName())
                .gpa(entity.getGpa())
                .academicStanding(entity.getAcademicStanding())
                .recordDate(entity.getRecordDate())
                .build();
    }

    private String calculateTrend(List<AcademicPerformance> records) {
        if (records.size() < 2) return "STABLE";
        BigDecimal current = records.get(0).getGpa();
        BigDecimal previous = records.get(1).getGpa();
        int comparison = current.compareTo(previous);
        if (comparison > 0) return "IMPROVING";
        if (comparison < 0) return "DECLINING";
        return "STABLE";
    }
}