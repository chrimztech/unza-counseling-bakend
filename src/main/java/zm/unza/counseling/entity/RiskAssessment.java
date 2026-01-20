package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private Integer riskScore;
    
    @Enumerated(EnumType.STRING)
    private Client.RiskLevel riskLevel;

    private LocalDateTime assessmentDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
}