package zm.unza.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for appointment statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStats {
    private long totalAppointments;
    private long todayAppointments;
    private long monthlyAppointments;
    private long scheduled;
    private long confirmed;
    private long completed;
    private long cancelled;
    private long pending;
    private long unassigned;
}
