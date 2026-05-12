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
@Table(name = "scholarships")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scholarship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String sponsor;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScholarshipType type;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "max_recipients")
    private Integer maxRecipients;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;

    @Column(name = "required_min_gpa")
    private Double requiredMinGpa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScholarshipStatus status = ScholarshipStatus.OPEN;

    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ScholarshipType {
        FINANCIAL_NEED,
        MERIT_BASED,
        VULNERABILITY_BASED,
        SPECIAL_CIRCUMSTANCES,
        GENERAL
    }

    public enum ScholarshipStatus {
        OPEN,
        CLOSED,
        AWARDED,
        CANCELLED
    }
}
