package zm.unza.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicPerformanceSummary {
    private Long clientId;
    private String clientName;
    private BigDecimal currentGpa;
    private BigDecimal averageGpa;
    private String currentStanding;
    private Integer totalSemesters;
    private BigDecimal averageAttendance;
    private String trend;
    private LocalDate lastRecordDate;
}