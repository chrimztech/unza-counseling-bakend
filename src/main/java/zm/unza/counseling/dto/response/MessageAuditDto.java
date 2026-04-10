package zm.unza.counseling.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageAuditDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private Long recipientId;
    private String recipientName;
    private String recipientEmail;
    private String subject;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime deletedAt;
    private boolean read;
    private boolean delivered;
    private boolean archived;
    private boolean starred;
    private boolean deletedBySender;
    private boolean deletedByRecipient;
}
