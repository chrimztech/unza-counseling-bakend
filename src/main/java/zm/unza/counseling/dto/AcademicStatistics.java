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
public class AcademicStatistics {
    private BigDecimal averageGpa;
    private BigDecimal averageAttendance;
    private Long totalStudents;
    private Long studentsAtRisk;
    private Long studentsOnProbation;
    private Long excellentPerformers;
    private Long decliningStudents;
}