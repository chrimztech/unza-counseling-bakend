package zm.unza.counseling.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "self_assessments")
@EntityListeners(AuditingEntityListener.class)
@Data
public class SelfAssessment {
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private String id;
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "client_id", nullable = false)
private Client client;
private Integer overallScore;
private Integer mentalHealthScore;
private Integer academicScore;
private Integer socialScore;
private Integer physicalScore;
@Enumerated(EnumType.STRING)
private RiskLevel assessedRiskLevel;
@OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
private List<AssessmentResponse> responses = new ArrayList<>();
@Column(length = 2000)
private String aiInsights;
@Column(length = 2000)
private String recommendations;@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime completedAt;
public enum RiskLevel {
LOW, MODERATE, HIGH, CRITICAL
}
}