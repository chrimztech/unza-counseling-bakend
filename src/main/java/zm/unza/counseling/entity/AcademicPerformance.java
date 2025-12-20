package zm.unza.counseling.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_performance")
public class AcademicPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "gpa", precision = 3, scale = 2)
    private BigDecimal gpa;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Column(name = "credits_completed")
    private Integer creditsCompleted;

    @Column(name = "credits_failed")
    private Integer creditsFailed;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Column(name = "assignments_completed")
    private Integer assignmentsCompleted;

    @Column(name = "assignments_total")
    private Integer assignmentsTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_standing")
    private AcademicStanding academicStanding;

    @Column(name = "courses_dropped")
    private Integer coursesDropped;

    @Column(name = "courses_withdrawn")
    private Integer coursesWithdrawn;

    @Column(name = "study_program")
    private String studyProgram;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "department")
    private String department;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public AcademicPerformance() {}

    // All args constructor
    public AcademicPerformance(Long id, Client client, String academicYear, Integer semester, BigDecimal gpa,
                              Integer totalCredits, Integer creditsCompleted, Integer creditsFailed,
                              BigDecimal attendanceRate, Integer assignmentsCompleted, Integer assignmentsTotal,
                              AcademicStanding academicStanding, Integer coursesDropped, Integer coursesWithdrawn,
                              String studyProgram, Integer yearOfStudy, String faculty, String department,
                              LocalDate recordDate, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.client = client;
        this.academicYear = academicYear;
        this.semester = semester;
        this.gpa = gpa;
        this.totalCredits = totalCredits;
        this.creditsCompleted = creditsCompleted;
        this.creditsFailed = creditsFailed;
        this.attendanceRate = attendanceRate;
        this.assignmentsCompleted = assignmentsCompleted;
        this.assignmentsTotal = assignmentsTotal;
        this.academicStanding = academicStanding;
        this.coursesDropped = coursesDropped;
        this.coursesWithdrawn = coursesWithdrawn;
        this.studyProgram = studyProgram;
        this.yearOfStudy = yearOfStudy;
        this.faculty = faculty;
        this.department = department;
        this.recordDate = recordDate;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public BigDecimal getGpa() { return gpa; }
    public void setGpa(BigDecimal gpa) { this.gpa = gpa; }

    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer totalCredits) { this.totalCredits = totalCredits; }

    public Integer getCreditsCompleted() { return creditsCompleted; }
    public void setCreditsCompleted(Integer creditsCompleted) { this.creditsCompleted = creditsCompleted; }

    public Integer getCreditsFailed() { return creditsFailed; }
    public void setCreditsFailed(Integer creditsFailed) { this.creditsFailed = creditsFailed; }

    public BigDecimal getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(BigDecimal attendanceRate) { this.attendanceRate = attendanceRate; }

    public Integer getAssignmentsCompleted() { return assignmentsCompleted; }
    public void setAssignmentsCompleted(Integer assignmentsCompleted) { this.assignmentsCompleted = assignmentsCompleted; }

    public Integer getAssignmentsTotal() { return assignmentsTotal; }
    public void setAssignmentsTotal(Integer assignmentsTotal) { this.assignmentsTotal = assignmentsTotal; }

    public AcademicStanding getAcademicStanding() { return academicStanding; }
    public void setAcademicStanding(AcademicStanding academicStanding) { this.academicStanding = academicStanding; }

    public Integer getCoursesDropped() { return coursesDropped; }
    public void setCoursesDropped(Integer coursesDropped) { this.coursesDropped = coursesDropped; }

    public Integer getCoursesWithdrawn() { return coursesWithdrawn; }
    public void setCoursesWithdrawn(Integer coursesWithdrawn) { this.coursesWithdrawn = coursesWithdrawn; }

    public String getStudyProgram() { return studyProgram; }
    public void setStudyProgram(String studyProgram) { this.studyProgram = studyProgram; }

    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper method to calculate completion rate
    public BigDecimal getCompletionRate() {
        if (totalCredits == null || totalCredits == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(creditsCompleted)
                .divide(BigDecimal.valueOf(totalCredits), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    // Helper method to calculate assignment completion rate
    public BigDecimal getAssignmentCompletionRate() {
        if (assignmentsTotal == null || assignmentsTotal == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(assignmentsCompleted)
                .divide(BigDecimal.valueOf(assignmentsTotal), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AcademicPerformance performance = new AcademicPerformance();

        public Builder id(Long id) {
            performance.setId(id);
            return this;
        }

        public Builder client(Client client) {
            performance.setClient(client);
            return this;
        }

        public Builder academicYear(String academicYear) {
            performance.setAcademicYear(academicYear);
            return this;
        }

        public Builder semester(Integer semester) {
            performance.setSemester(semester);
            return this;
        }

        public Builder gpa(BigDecimal gpa) {
            performance.setGpa(gpa);
            return this;
        }

        public Builder totalCredits(Integer totalCredits) {
            performance.setTotalCredits(totalCredits);
            return this;
        }

        public Builder creditsCompleted(Integer creditsCompleted) {
            performance.setCreditsCompleted(creditsCompleted);
            return this;
        }

        public Builder creditsFailed(Integer creditsFailed) {
            performance.setCreditsFailed(creditsFailed);
            return this;
        }

        public Builder attendanceRate(BigDecimal attendanceRate) {
            performance.setAttendanceRate(attendanceRate);
            return this;
        }

        public Builder assignmentsCompleted(Integer assignmentsCompleted) {
            performance.setAssignmentsCompleted(assignmentsCompleted);
            return this;
        }

        public Builder assignmentsTotal(Integer assignmentsTotal) {
            performance.setAssignmentsTotal(assignmentsTotal);
            return this;
        }

        public Builder academicStanding(AcademicStanding academicStanding) {
            performance.setAcademicStanding(academicStanding);
            return this;
        }

        public Builder coursesDropped(Integer coursesDropped) {
            performance.setCoursesDropped(coursesDropped);
            return this;
        }

        public Builder coursesWithdrawn(Integer coursesWithdrawn) {
            performance.setCoursesWithdrawn(coursesWithdrawn);
            return this;
        }

        public Builder studyProgram(String studyProgram) {
            performance.setStudyProgram(studyProgram);
            return this;
        }

        public Builder yearOfStudy(Integer yearOfStudy) {
            performance.setYearOfStudy(yearOfStudy);
            return this;
        }

        public Builder faculty(String faculty) {
            performance.setFaculty(faculty);
            return this;
        }

        public Builder department(String department) {
            performance.setDepartment(department);
            return this;
        }

        public Builder recordDate(LocalDate recordDate) {
            performance.setRecordDate(recordDate);
            return this;
        }

        public Builder notes(String notes) {
            performance.setNotes(notes);
            return this;
        }

        public AcademicPerformance build() {
            return performance;
        }
    }

    public enum AcademicStanding {
        EXCELLENT,           // GPA 3.5+
        GOOD,               // GPA 3.0-3.49
        SATISFACTORY,       // GPA 2.5-2.99
        PROBATION,          // GPA 2.0-2.49
        ACADEMIC_WARNING,   // GPA 1.5-1.99
        DISMISSED,          // GPA below 1.5
        NOT_DETERMINED
    }
}