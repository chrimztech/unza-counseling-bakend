package zm.unza.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicVisitFrequencyResponse {

    private Long clientId;
    private String clientName;
    private String clientEmail;

    private long totalVisits;
    private long visitsLast30Days;
    private long visitsLast90Days;
    private long visitsThisYear;

    // "FREQUENT" when >= 3 visits in any rolling 90-day window
    private boolean frequentVisitor;

    // month -> count, e.g. {"2026-03": 2, "2026-04": 1}
    private Map<String, Long> visitsByMonth;

    private List<ClinicVisitResponse> recentVisits;
}
