package zm.unza.counseling.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalClients;
    private long activeClients;
    private long totalSessions;
    private long pendingAppointments;
    private long highRiskClients;
    private long totalCounselors;
    private double averageSatisfaction;
}
