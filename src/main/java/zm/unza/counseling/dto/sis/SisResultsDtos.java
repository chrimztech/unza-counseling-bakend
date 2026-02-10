package zm.unza.counseling.dto.sis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Student Information System (SIS) Results API Response
 * Based on the Flutter ResultsService implementation
 */
public class SisResultsDtos {

    /**
     * Main response wrapper from SIS API
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SisResultsResponse {
        private String status;
        private SisResultsData data;
        private String message;
        
        public boolean isSuccess() {
            return "success".equalsIgnoreCase(status);
        }
    }

    /**
     * Data wrapper containing student course history
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SisResultsData {
        @JsonProperty("student_course_history")
        private List<StudentCourseHistory> studentCourseHistory;
        
        @JsonProperty("student_info")
        private StudentInfo studentInfo;
    }

    /**
     * Individual course result item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentCourseHistory {
        @JsonProperty("course_code")
        private String courseCode;
        
        @JsonProperty("course_title")
        private String courseTitle;
        
        @JsonProperty("credit_hours")
        private Integer creditHours;
        
        @JsonProperty("semester")
        private String semester;
        
        @JsonProperty("academic_year")
        private String academicYear;
        
        @JsonProperty("grade")
        private String grade;
        
        @JsonProperty("grade_point")
        private BigDecimal gradePoint;
        
        @JsonProperty("marks")
        private BigDecimal marks;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("course_type")
        private String courseType;
        
        @JsonProperty("assessment_type")
        private String assessmentType;
    }

    /**
     * Student information from SIS
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentInfo {
        @JsonProperty("student_id")
        private String studentId;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        @JsonProperty("programme")
        private String programme;
        
        @JsonProperty("faculty")
        private String faculty;
        
        @JsonProperty("department")
        private String department;
        
        @JsonProperty("year_of_study")
        private Integer yearOfStudy;
        
        @JsonProperty("current_gpa")
        private BigDecimal currentGpa;
        
        @JsonProperty("cumulative_gpa")
        private BigDecimal cumulativeGpa;
        
        @JsonProperty("total_credits_earned")
        private Integer totalCreditsEarned;
        
        @JsonProperty("total_credits_attempted")
        private Integer totalCreditsAttempted;
        
        @JsonProperty("classification")
        private String classification;
    }

    /**
     * Summary statistics calculated from results
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultsSummary {
        private Integer totalCourses;
        private Integer passedCourses;
        private Integer failedCourses;
        private Integer withdrawnCourses;
        private Integer incompleteCourses;
        private Double averageGrade;
        private Double averageGpa;
        private BigDecimal currentGpa;
        private BigDecimal cumulativeGpa;
        private Integer totalCreditsEarned;
        private Integer totalCreditsAttempted;
        private String academicStanding;
        private String performanceTrend;
    }

    /**
     * Request to sync results from SIS
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncResultsRequest {
        private String studentId;
        private String token;
        private Boolean forceRefresh;
    }

    /**
     * Response after syncing results
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncResultsResponse {
        private boolean success;
        private String message;
        private String errorType;
        private ResultsSummary summary;
        private List<StudentCourseHistory> courses;
        private StudentInfo studentInfo;
    }
}
