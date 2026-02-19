package zm.unza.counseling.dto.request;

import lombok.Data;

/**
 * Request DTO for cancelling an appointment
 */
@Data
public class CancelRequest {
    private String reason;
}
