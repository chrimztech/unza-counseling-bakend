package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PanicButtonRequest {

    private Double latitude;

    private Double longitude;
}
