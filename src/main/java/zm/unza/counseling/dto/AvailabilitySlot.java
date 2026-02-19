package zm.unza.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing an availability slot for a counselor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySlot {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;
}
