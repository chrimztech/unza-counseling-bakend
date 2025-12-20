package zm.unza.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAtRiskDto {
    private Long clientId;
    private String clientName;
    private String studentNumber;
    private BigDecimal gpa;
    private String academicStanding;
    private BigDecimal attendanceRate;
    private String faculty;
    private String riskLevel;
    private String recommendedAction;
}