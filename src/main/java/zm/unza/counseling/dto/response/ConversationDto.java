package zm.unza.counseling.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Enhanced Conversation DTO for messaging between counselor and client
 */
@Data
public class ConversationDto {
    private Long conversationId;
    private Long partnerId;
    private String partnerUsername;
    private String partnerEmail;
    private String partnerFirstName;
    private String partnerLastName;
    private String partnerFullName;
    private String partnerProfilePicture;
    private String partnerType; // "COUNSELOR" or "CLIENT"
    private String partnerSpecialization; // For counselors
    private String partnerStudentId; // For clients
    private String partnerProgramme; // For clients
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private boolean isOnline;
    private LocalDateTime lastSeen;
}
