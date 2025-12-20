package zm.unza.counseling.dto.request;

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
public class AcademicPerformanceRequest {
    private Long clientId;
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
    private String notes;
}