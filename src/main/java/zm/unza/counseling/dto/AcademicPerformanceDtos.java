package zm.unza.counseling.dto;

import lombok.Data;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AcademicPerformanceDtos {
    @Data
    public static class AcademicPerformanceSummary {
        private Double currentGpa;
        private Double gpaChange;
        private String academicStatus;

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final AcademicPerformanceSummary object = new AcademicPerformanceSummary();
            public Builder currentGpa(Double val) { object.setCurrentGpa(val); return this; }
            public Builder gpaChange(Double val) { object.setGpaChange(val); return this; }
            public Builder academicStatus(String val) { object.setAcademicStatus(val); return this; }
            public AcademicPerformanceSummary build() { return object; }
        }
    }

    @Data
    public static class GpaTrendData {
        private List<Object> trendPoints;

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final GpaTrendData object = new GpaTrendData();
            public Builder trendPoints(List<Object> val) { object.setTrendPoints(val); return this; }
            public GpaTrendData build() { return object; }
        }
    }

    @Data
    public static class StudentAtRiskDto {
        private Long studentId;
        private String riskLevel;

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final StudentAtRiskDto object = new StudentAtRiskDto();
            public Builder studentId(Long val) { object.setStudentId(val); return this; }
            public Builder riskLevel(String val) { object.setRiskLevel(val); return this; }
            public StudentAtRiskDto build() { return object; }
        }
    }

    @Data
    public static class AcademicStatistics {
        private Double averageGpa;
        private Integer totalStudents;

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final AcademicStatistics object = new AcademicStatistics();
            public Builder averageGpa(Double val) { object.setAverageGpa(val); return this; }
            public Builder totalStudents(Integer val) { object.setTotalStudents(val); return this; }
            public AcademicStatistics build() { return object; }
        }
    }

    @Data
    public static class AcademicPerformanceRequest {
        private Long studentId;
        private Double gpa;
        private Integer yearOfStudy;
        private String semester;
        private String programme;
        private Double attendanceRate;
        
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public Double getGpa() { return gpa; }
        public void setGpa(Double gpa) { this.gpa = gpa; }
        public Integer getYearOfStudy() { return yearOfStudy; }
        public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
        public String getProgramme() { return programme; }
        public void setProgramme(String programme) { this.programme = programme; }
        public Double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class AcademicPerformanceResponse extends AcademicPerformanceRequest {}
}