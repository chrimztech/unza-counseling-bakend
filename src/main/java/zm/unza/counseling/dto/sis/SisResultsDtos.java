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
 * Based on the actual SIS API response structure
 */
public class SisResultsDtos {

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentCourseHistory {
        @JsonProperty("IntakeBatch")
        private IntakeBatch intakeBatch;
        
        @JsonProperty("Program")
        private Program program;
        
        @JsonProperty("Study")
        private Study study;
        
        @JsonProperty("Course")
        private Course course;
        
        @JsonProperty("Session")
        private Session session;
        
        @JsonProperty("StudentCourse")
        private StudentCourse studentCourse;
        
        @JsonProperty("StudentEndcomment")
        private StudentEndcomment studentEndcomment;
        
        @JsonProperty("Student")
        private Student student;
        
        @JsonProperty("StudentStatus")
        private StudentStatus studentStatus;
        
        @JsonProperty("Comment")
        private Comment comment;
        
        @JsonProperty("Grades")
        private Grades grades;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntakeBatch {
        @JsonProperty("period_name")
        private String periodName;
        
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("intake_id")
        private String intakeId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Program {
        @JsonProperty("program_description")
        private String programDescription;
        
        @JsonProperty("program_abbrev")
        private String programAbbrev;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Study {
        @JsonProperty("study_description")
        private String studyDescription;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Course {
        @JsonProperty("course_code")
        private String courseCode;
        
        @JsonProperty("credits")
        private String credits;
        
        @JsonProperty("course_description")
        private String courseDescription;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Session {
        @JsonProperty("session_code")
        private String sessionCode;
        
        @JsonProperty("session_end")
        private String sessionEnd;
        
        @JsonProperty("id")
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentCourse {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("intake_id")
        private String intakeId;
        
        @JsonProperty("tmp_final_grade")
        private String tmpFinalGrade;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentEndcomment {
        @JsonProperty("results_withheld")
        private String resultsWithheld;
        
        @JsonProperty("results_published")
        private String resultsPublished;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Student {
        @JsonProperty("student_id")
        private String studentId;
        
        @JsonProperty("surname")
        private String surname;
        
        @JsonProperty("middle_name")
        private String middleName;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("gender")
        private String gender;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentStatus {
        @JsonProperty("status_id")
        private String statusId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Comment {
        @JsonProperty("comment_descr")
        private String commentDescr;
        
        @JsonProperty("comment_code")
        private String commentCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Grades {
        @JsonProperty("grade_code")
        private String gradeCode;
        
        @JsonProperty("gradepoint")
        private String gradepoint;
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncResultsRequest {
        private String studentId;
        private String token;
        private Boolean forceRefresh;
    }

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
