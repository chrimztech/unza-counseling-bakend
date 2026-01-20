package zm.unza.counseling.dto.request;

import lombok.Data;

@Data
public class MessageRequest {
    private Long recipientId;
    private String subject;
    private String content;
}
