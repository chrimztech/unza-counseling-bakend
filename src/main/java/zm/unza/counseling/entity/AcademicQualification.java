package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for storing academic qualifications/course results fetched from SIS
 * Used for mental health and academic performance analysis
 */
@Entity
@Table(name = "academic_qualifications")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    // Course Information
    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "course_title")
    private String courseTitle;

    @Column(name = "credit_hours")
    private Integer creditHours;

    @Column(name = "semester")
    private String semester;

    @Column(name = "academic_year")
    private String academicYear;

    // Grade Information
    @Column(name = "grade")
    private String grade;

    @Column(name = "grade_point")
    private BigDecimal gradePoint;

    @Column(name = "marks")
    private BigDecimal marks;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status")
    private CourseStatus courseStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type")
    private CourseType courseType;

    // Academic Standing
    @Column(name = "current_gpa")
    private BigDecimal currentGpa;

    @Column(name = "cumulative_gpa")
    private BigDecimal cumulativeGpa;

    @Column(name = "total_credits_earned")
    private Integer totalCreditsEarned;

    @Column(name = "total_credits_attempted")
    private Integer totalCreditsAttempted;

    @Column(name = "academic_standing")
    private String academicStanding;

    // Sync metadata
    @Column(name = "sis_sync_date")
    private LocalDateTime sisSyncDate;

    @Column(name = "external_data_hash")
    private String externalDataHash;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Course status enum
     */
    public enum CourseStatus {
        PASSED,
        FAILED,
        INCOMPLETE,
        WITHDRAWN,
        IN_PROGRESS,
        NOT_YET_ATTEMPTED
    }

    /**
     * Course type enum
     */
    public enum CourseType {
        CORE,
        ELECTIVE,
        PREREQUISITE,
        GENERAL
    }

    /**
     * Helper method to determine if course was passed
     */
    public boolean isPassed() {
        if (gradePoint == null) return false;
        return gradePoint.compareTo(new BigDecimal("2.0")) >= 0;
    }

    /**
     * Helper method to calculate weighted grade point
     */
    public BigDecimal getWeightedGradePoint() {
        if (gradePoint == null || creditHours == null) {
            return BigDecimal.ZERO;
        }
        return gradePoint.multiply(new BigDecimal(creditHours));
    }
}
