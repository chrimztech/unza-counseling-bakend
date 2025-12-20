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
public class FacultyPerformanceReport {
    private String faculty;
    private BigDecimal averageGpa;
    private Long studentCount;
    private Long atRiskCount;
    private BigDecimal averageAttendance;
}