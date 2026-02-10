package zm.unza.counseling.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import zm.unza.counseling.dto.sis.SisResultsDtos.*;
import zm.unza.counseling.entity.AcademicQualification;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.AcademicQualificationRepository;
import zm.unza.counseling.repository.ClientRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for fetching and syncing academic qualifications from the external SIS API
 * Based on the Flutter ResultsService implementation
 */
@Service
@Transactional
public class SisResultsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ClientRepository clientRepository;
    private final AcademicQualificationRepository academicQualificationRepository;

    @Autowired
    public SisResultsService(RestTemplate restTemplate, ObjectMapper objectMapper,
                             ClientRepository clientRepository,
                             AcademicQualificationRepository academicQualificationRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.clientRepository = clientRepository;
        this.academicQualificationRepository = academicQualificationRepository;
    }

    @Value("${app.sis.api.baseUrl:https://sis.unza.zm}")
    private String sisBaseUrl;

    @Value("${app.sis.api.resultsEndpoint:/StudentApp/getStudentResults.json}")
    private String resultsEndpoint;

    /**
     * Fetch and sync student results from SIS API
     */
    public SyncResultsResponse fetchStudentResults(String studentId, String token, boolean forceRefresh) {
        System.out.println("Fetching results for student: " + studentId + " from SIS API");
        
        try {
            // Validate token if not forcing refresh
            if (!forceRefresh && (token == null || token.isEmpty())) {
                return SyncResultsResponse.builder()
                        .success(false)
                        .message("Authentication token is required")
                        .errorType("auth")
                        .build();
            }

            // Build the request URL
            String url = buildResultsUrl(studentId, token);
            System.out.println("SIS API URL: " + url);

            // Make the API request
            ResponseEntity<String> response = makeRequest(url);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                return handleHttpError(response.getStatusCode());
            }

            // Parse the response
            SisResultsResponse sisResponse = parseResponse(response.getBody());
            
            if (sisResponse == null || !sisResponse.isSuccess()) {
                return SyncResultsResponse.builder()
                        .success(false)
                        .message(sisResponse != null ? sisResponse.getMessage() : "Failed to parse response")
                        .errorType("server")
                        .build();
            }

            // Process and store the results
            return processResults(sisResponse, studentId);

        } catch (RestClientException e) {
            System.out.println("Network error fetching SIS results: " + e.getMessage());
            return SyncResultsResponse.builder()
                    .success(false)
                    .message("Unable to connect to SIS. Please check your network settings.")
                    .errorType("network")
                    .build();
        } catch (Exception e) {
            System.out.println("Error fetching SIS results: " + e.getMessage());
            e.printStackTrace();
            return SyncResultsResponse.builder()
                    .success(false)
                    .message("An unexpected error occurred while fetching results")
                    .errorType("unknown")
                    .build();
        }
    }

    /**
     * Fetch results for a client by their internal ID
     */
    public SyncResultsResponse fetchResultsForClient(Long clientId, String token, boolean forceRefresh) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));
        
        if (client.getStudentId() == null || client.getStudentId().isEmpty()) {
            return SyncResultsResponse.builder()
                    .success(false)
                    .message("Client does not have a student ID associated")
                    .errorType("validation")
                    .build();
        }
        
        return fetchStudentResults(client.getStudentId(), token, forceRefresh);
    }

    /**
     * Get cached results for a client
     */
    public SyncResultsResponse getCachedResults(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));
        
        List<AcademicQualification> qualifications = academicQualificationRepository
                .findByClientIdAndIsActiveTrueOrderByAcademicYearDescSemesterDesc(clientId);
        
        if (qualifications.isEmpty()) {
            return SyncResultsResponse.builder()
                    .success(false)
                    .message("No cached results found for this client")
                    .errorType("notFound")
                    .build();
        }

        // Calculate summary from cached data
        ResultsSummary summary = calculateSummaryFromQualifications(qualifications);
        
        // Convert to course history list
        List<StudentCourseHistory> courses = qualifications.stream()
                .map(this::convertToCourseHistory)
                .collect(Collectors.toList());

        // Get latest student info
        AcademicQualification latest = qualifications.get(0);
        StudentInfo studentInfo = StudentInfo.builder()
                .studentId(client.getStudentId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .programme(client.getProgramme())
                .faculty(client.getFaculty())
                .yearOfStudy(client.getYearOfStudy())
                .currentGpa(latest.getCurrentGpa())
                .cumulativeGpa(latest.getCumulativeGpa())
                .totalCreditsEarned(latest.getTotalCreditsEarned())
                .totalCreditsAttempted(latest.getTotalCreditsAttempted())
                .build();

        return SyncResultsResponse.builder()
                .success(true)
                .message("Cached results retrieved successfully")
                .summary(summary)
                .courses(courses)
                .studentInfo(studentInfo)
                .build();
    }

    // ============ Private Helper Methods ============

    /**
     * Build the results URL
     */
    private String buildResultsUrl(String studentId, String token) {
        if (sisBaseUrl == null || sisBaseUrl.isEmpty()) {
            throw new IllegalStateException("SIS base URL is not configured");
        }

        return UriComponentsBuilder.fromHttpUrl(sisBaseUrl + resultsEndpoint)
                .queryParam("student_id", studentId)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    /**
     * Make HTTP request to SIS API
     */
    private ResponseEntity<String> makeRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    /**
     * Parse the SIS API response
     */
    private SisResultsResponse parseResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            SisResultsResponse.SisResultsResponseBuilder builder = SisResultsResponse.builder();
            
            if (jsonNode.has("status")) {
                builder.status(jsonNode.get("status").asText());
            }
            
            if (jsonNode.has("message")) {
                builder.message(jsonNode.get("message").asText());
            }
            
            if (jsonNode.has("data") && !jsonNode.get("data").isNull()) {
                JsonNode dataNode = jsonNode.get("data");
                SisResultsData.SisResultsDataBuilder dataBuilder = SisResultsData.builder();
                
                if (dataNode.has("student_course_history") && !dataNode.get("student_course_history").isNull()) {
                    List<StudentCourseHistory> courses = objectMapper.readValue(
                            dataNode.get("student_course_history").toString(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, StudentCourseHistory.class)
                    );
                    dataBuilder.studentCourseHistory(courses);
                }
                
                if (dataNode.has("student_info") && !dataNode.get("student_info").isNull()) {
                    StudentInfo studentInfo = objectMapper.treeToValue(dataNode.get("student_info"), StudentInfo.class);
                    dataBuilder.studentInfo(studentInfo);
                }
                
                builder.data(dataBuilder.build());
            }
            
            return builder.build();
            
        } catch (Exception e) {
            System.out.println("Error parsing SIS response: " + e.getMessage());
            return null;
        }
    }

    /**
     * Process and store results in the database
     */
    private SyncResultsResponse processResults(SisResultsResponse sisResponse, String studentId) {
        Client client = clientRepository.findByStudentId(studentId)
                .orElse(null);
        
        if (client == null) {
            // Return results without storing if client not found
            ResultsSummary summary = calculateSummary(sisResponse.getData());
            return SyncResultsResponse.builder()
                    .success(true)
                    .message("Results retrieved from SIS but client not found in counseling system")
                    .summary(summary)
                    .courses(sisResponse.getData() != null ? sisResponse.getData().getStudentCourseHistory() : null)
                    .studentInfo(sisResponse.getData() != null ? sisResponse.getData().getStudentInfo() : null)
                    .build();
        }

        // Deactivate old qualifications
        academicQualificationRepository.deactivateQualificationsByClientId(client.getId());

        // Store new qualifications
        List<StudentCourseHistory> courses = sisResponse.getData() != null ? 
                sisResponse.getData().getStudentCourseHistory() : Collections.emptyList();
        
        List<AcademicQualification> savedQualifications = new ArrayList<>();
        
        for (StudentCourseHistory course : courses) {
            AcademicQualification qualification = convertToEntity(course, client, studentId);
            savedQualifications.add(academicQualificationRepository.save(qualification));
        }

        // Update client with latest academic info
        updateClientAcademicInfo(client, sisResponse.getData());

        // Calculate summary
        ResultsSummary summary = calculateSummaryFromQualifications(savedQualifications);

        return SyncResultsResponse.builder()
                .success(true)
                .message("Results synced successfully from SIS")
                .summary(summary)
                .courses(courses)
                .studentInfo(sisResponse.getData() != null ? sisResponse.getData().getStudentInfo() : null)
                .build();
    }

    /**
     * Convert SIS response to AcademicQualification entity
     */
    private AcademicQualification convertToEntity(StudentCourseHistory course, Client client, String studentId) {
        AcademicQualification qualification = new AcademicQualification();
        qualification.setClient(client);
        qualification.setStudentId(studentId);
        qualification.setCourseCode(course.getCourseCode());
        qualification.setCourseTitle(course.getCourseTitle());
        qualification.setCreditHours(course.getCreditHours());
        qualification.setSemester(course.getSemester());
        qualification.setAcademicYear(course.getAcademicYear());
        qualification.setGrade(course.getGrade());
        qualification.setGradePoint(course.getGradePoint());
        qualification.setMarks(course.getMarks());
        
        // Set course status
        if (course.getStatus() != null) {
            try {
                qualification.setCourseStatus(AcademicQualification.CourseStatus.valueOf(course.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                qualification.setCourseStatus(AcademicQualification.CourseStatus.INCOMPLETE);
            }
        }
        
        // Set course type
        if (course.getCourseType() != null) {
            try {
                qualification.setCourseType(AcademicQualification.CourseType.valueOf(course.getCourseType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                qualification.setCourseType(AcademicQualification.CourseType.GENERAL);
            }
        }
        
        qualification.setSisSyncDate(LocalDateTime.now());
        qualification.setIsActive(true);
        
        return qualification;
    }

    /**
     * Update client with latest academic info from SIS
     */
    private void updateClientAcademicInfo(Client client, SisResultsData data) {
        if (data != null && data.getStudentInfo() != null) {
            StudentInfo info = data.getStudentInfo();
            
            if (info.getCurrentGpa() != null) {
                client.setGpa(info.getCurrentGpa().doubleValue());
            }
            if (info.getProgramme() != null && (client.getProgramme() == null || client.getProgramme().isEmpty())) {
                client.setProgramme(info.getProgramme());
            }
            if (info.getFaculty() != null && (client.getFaculty() == null || client.getFaculty().isEmpty())) {
                client.setFaculty(info.getFaculty());
            }
            if (info.getYearOfStudy() != null && client.getYearOfStudy() == null) {
                client.setYearOfStudy(info.getYearOfStudy());
            }
            
            clientRepository.save(client);
        }
    }

    /**
     * Convert entity to course history DTO
     */
    private StudentCourseHistory convertToCourseHistory(AcademicQualification qualification) {
        return StudentCourseHistory.builder()
                .courseCode(qualification.getCourseCode())
                .courseTitle(qualification.getCourseTitle())
                .creditHours(qualification.getCreditHours())
                .semester(qualification.getSemester())
                .academicYear(qualification.getAcademicYear())
                .grade(qualification.getGrade())
                .gradePoint(qualification.getGradePoint())
                .marks(qualification.getMarks())
                .status(qualification.getCourseStatus() != null ? qualification.getCourseStatus().name() : null)
                .courseType(qualification.getCourseType() != null ? qualification.getCourseType().name() : null)
                .build();
    }

    /**
     * Calculate summary from SIS data
     */
    private ResultsSummary calculateSummary(SisResultsData data) {
        if (data == null || data.getStudentCourseHistory() == null) {
            return ResultsSummary.builder().build();
        }

        List<StudentCourseHistory> courses = data.getStudentCourseHistory();
        
        int totalCourses = courses.size();
        int passedCourses = (int) courses.stream()
                .filter(c -> "PASSED".equalsIgnoreCase(c.getStatus()))
                .count();
        int failedCourses = (int) courses.stream()
                .filter(c -> "FAILED".equalsIgnoreCase(c.getStatus()))
                .count();
        int withdrawnCourses = (int) courses.stream()
                .filter(c -> "WITHDRAWN".equalsIgnoreCase(c.getStatus()))
                .count();
        
        Double avgGpa = courses.stream()
                .filter(c -> c.getGradePoint() != null)
                .mapToDouble(c -> c.getGradePoint().doubleValue())
                .average()
                .orElse(0.0);

        String academicStanding = determineAcademicStanding(avgGpa);

        return ResultsSummary.builder()
                .totalCourses(totalCourses)
                .passedCourses(passedCourses)
                .failedCourses(failedCourses)
                .withdrawnCourses(withdrawnCourses)
                .averageGpa(avgGpa)
                .academicStanding(academicStanding)
                .build();
    }

    /**
     * Calculate summary from stored qualifications
     */
    private ResultsSummary calculateSummaryFromQualifications(List<AcademicQualification> qualifications) {
        if (qualifications.isEmpty()) {
            return ResultsSummary.builder().build();
        }

        int totalCourses = qualifications.size();
        int passedCourses = (int) qualifications.stream()
                .filter(q -> q.getCourseStatus() == AcademicQualification.CourseStatus.PASSED)
                .count();
        int failedCourses = (int) qualifications.stream()
                .filter(q -> q.getCourseStatus() == AcademicQualification.CourseStatus.FAILED)
                .count();
        int withdrawnCourses = (int) qualifications.stream()
                .filter(q -> q.getCourseStatus() == AcademicQualification.CourseStatus.WITHDRAWN)
                .count();

        Double avgGpa = qualifications.stream()
                .filter(q -> q.getGradePoint() != null)
                .mapToDouble(q -> q.getGradePoint().doubleValue())
                .average()
                .orElse(0.0);

        BigDecimal currentGpa = qualifications.get(0).getCurrentGpa();
        BigDecimal cumulativeGpa = qualifications.get(0).getCumulativeGpa();

        return ResultsSummary.builder()
                .totalCourses(totalCourses)
                .passedCourses(passedCourses)
                .failedCourses(failedCourses)
                .withdrawnCourses(withdrawnCourses)
                .averageGpa(avgGpa)
                .currentGpa(currentGpa)
                .cumulativeGpa(cumulativeGpa)
                .academicStanding(determineAcademicStanding(avgGpa))
                .performanceTrend(calculatePerformanceTrend(qualifications))
                .build();
    }

    /**
     * Determine academic standing based on GPA
     */
    private String determineAcademicStanding(Double gpa) {
        if (gpa >= 3.5) return "EXCELLENT";
        if (gpa >= 3.0) return "GOOD";
        if (gpa >= 2.5) return "SATISFACTORY";
        if (gpa >= 2.0) return "PASS";
        if (gpa >= 1.0) return "AT_RISK";
        return "ACADEMIC_PROBATION";
    }

    /**
     * Calculate performance trend from historical data
     */
    private String calculatePerformanceTrend(List<AcademicQualification> qualifications) {
        if (qualifications.size() < 2) return "STABLE";
        
        // Group by academic year and semester
        Map<String, List<AcademicQualification>> grouped = qualifications.stream()
                .collect(Collectors.groupingBy(q -> q.getAcademicYear() + "_" + q.getSemester()));
        
        List<Double> semesterGpas = new ArrayList<>();
        for (List<AcademicQualification> group : grouped.values()) {
            Double avgGpa = group.stream()
                    .filter(q -> q.getGradePoint() != null)
                    .mapToDouble(q -> q.getGradePoint().doubleValue())
                    .average()
                    .orElse(0.0);
            semesterGpas.add(avgGpa);
        }
        
        if (semesterGpas.size() < 2) return "STABLE";
        
        double recent = semesterGpas.get(0);
        double previous = semesterGpas.get(1);
        
        if (recent > previous + 0.2) return "IMPROVING";
        if (recent < previous - 0.2) return "DECLINING";
        return "STABLE";
    }

    /**
     * Handle HTTP errors
     */
    private SyncResultsResponse handleHttpError(HttpStatusCode status) {
        int statusCode = status.value();
        String errorType;
        String message;

        if (statusCode == 401 || statusCode == 403) {
            errorType = "auth";
            message = "Session expired. Please log in again.";
        } else if (statusCode == 404) {
            errorType = "notFound";
            message = "Results not found for this student.";
        } else if (statusCode == 500 || statusCode == 503) {
            errorType = "server";
            message = "SIS server is temporarily unavailable. Please try again later.";
        } else {
            errorType = "http";
            message = "Request failed with status: " + status;
        }

        return SyncResultsResponse.builder()
                .success(false)
                .message(message)
                .errorType(errorType)
                .build();
    }
}
