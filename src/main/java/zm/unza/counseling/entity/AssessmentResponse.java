package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assessment_responses")
@EntityListeners(AuditingEntityListener.class)
public class AssessmentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String question;

    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_assessment_id", nullable = false)
    private SelfAssessment assessment;
    
    public void setAssessment(SelfAssessment assessment) { this.assessment = assessment; }
    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}