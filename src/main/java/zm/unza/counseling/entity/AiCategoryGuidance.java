package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Per-category response direction from the template's "Guidance_Examples" sheet, e.g.
 * Crisis/Suicidal → "Immediate escalation. Express care, ask if they are safe now ...".
 * Always included in the wellness assistant's instructions.
 */
@Entity
@Table(name = "ai_category_guidance")
@Data
public class AiCategoryGuidance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_category", nullable = false, length = 100)
    private String problemCategory;

    @Column(name = "example_opening", length = 500)
    private String exampleOpening;

    @Column(name = "response_direction", columnDefinition = "TEXT", nullable = false)
    private String responseDirection;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
