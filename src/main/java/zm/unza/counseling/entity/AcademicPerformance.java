package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_performance")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private String academicYear;
    private Integer semester;
    private BigDecimal gpa;
    private Integer totalCredits;
    private Integer creditsCompleted;
    private Integer creditsFailed;
    private BigDecimal attendanceRate;
    private Integer assignmentsCompleted;
    private Integer assignmentsTotal;
    private String academicStanding;
    private Integer coursesDropped;
    private Integer coursesWithdrawn;
    private String studyProgram;
    private Integer yearOfStudy;
    private String faculty;
    private String department;
    private LocalDate recordDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}