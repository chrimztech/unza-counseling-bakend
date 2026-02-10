package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import zm.unza.counseling.dto.request.MessageRequest;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.MessageService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Controller for real-time messaging between counselor and client
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserRepository userRepository;

    /**
     * Handle new message sent via WebSocket
     */
    @MessageMapping("/message.send/{recipientId}")
    public void sendMessage(@DestinationVariable Long recipientId, 
                           Map<String, Object> messageData, 
                           Principal principal) {
        try {
            String senderEmail = principal.getName();
            User sender = userRepository.findByEmail(senderEmail).orElseThrow();
            
            // Create message via service
            MessageRequest msgRequest = new MessageRequest();
            msgRequest.setRecipientId(recipientId);
            msgRequest.setSubject((String) messageData.getOrDefault("subject", ""));
            msgRequest.setContent((String) messageData.get("content"));
            
            Message savedMessage = messageService.sendMessage(sender.getId(), msgRequest);
            
            // Send to recipient's queue
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(), 
                "/queue/messages", 
                savedMessage
            );
            
            // Broadcast conversation update
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_MESSAGE");
            notification.put("message", savedMessage);
            notification.put("senderId", sender.getId());
            notification.put("senderName", sender.getFullName());
            
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(), 
                "/queue/notifications", 
                notification
            );
            
            log.info("WebSocket message sent from {} to {}", sender.getEmail(), recipientId);
            
        } catch (Exception e) {
            log.error("Error sending WebSocket message: {}", e.getMessage());
        }
    }

    /**
     * Handle typing indicator
     */
    @MessageMapping("/typing.start/{partnerId}")
    public void sendTypingIndicator(@DestinationVariable Long partnerId, 
                                    Map<String, Object> data,
                                    Principal principal) {
        try {
            String senderEmail = principal.getName();
            User sender = userRepository.findByEmail(senderEmail).orElseThrow();
            
            Map<String, Object> typingInfo = new HashMap<>();
            typingInfo.put("type", "TYPING_START");
            typingInfo.put("partnerId", sender.getId());
            typingInfo.put("partnerName", sender.getFullName());
            typingInfo.put("timestamp", LocalDateTime.now());
            
            messagingTemplate.convertAndSendToUser(
                partnerId.toString(), 
                "/queue/typing", 
                typingInfo
            );
        } catch (Exception e) {
            log.error("Error sending typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Handle stop typing indicator
     */
    @MessageMapping("/typing.stop/{partnerId}")
    public void sendStopTypingIndicator(@DestinationVariable Long partnerId,
                                        Principal principal) {
        try {
            String senderEmail = principal.getName();
            User sender = userRepository.findByEmail(senderEmail).orElseThrow();
            
            Map<String, Object> typingInfo = new HashMap<>();
            typingInfo.put("type", "TYPING_STOP");
            typingInfo.put("partnerId", sender.getId());
            
            messagingTemplate.convertAndSendToUser(
                partnerId.toString(), 
                "/queue/typing", 
                typingInfo
            );
        } catch (Exception e) {
            log.error("Error sending stop typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Handle message delivery status
     */
    @MessageMapping("/message.delivered/{messageId}")
    public void markMessageDelivered(@DestinationVariable Long messageId,
                                    Map<String, Object> data,
                                    Principal principal) {
        try {
            String senderEmail = principal.getName();
            User recipient = userRepository.findByEmail(senderEmail).orElseThrow();
            
            Message message = messageService.getMessageById(messageId, recipient.getId());
            messageService.markMessageAsDelivered(messageId, recipient.getId());
            
            // Notify sender that message was delivered
            Map<String, Object> deliveryStatus = new HashMap<>();
            deliveryStatus.put("type", "DELIVERED");
            deliveryStatus.put("messageId", messageId);
            deliveryStatus.put("deliveredAt", LocalDateTime.now());
            
            messagingTemplate.convertAndSendToUser(
                message.getSender().getId().toString(), 
                "/queue/delivery", 
                deliveryStatus
            );
        } catch (Exception e) {
            log.error("Error marking message as delivered: {}", e.getMessage());
        }
    }

    /**
     * Handle online status updates
     */
    @MessageMapping("/status.online")
    public void broadcastOnlineStatus(Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElseThrow();
            
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("type", "ONLINE");
            statusUpdate.put("userId", user.getId());
            statusUpdate.put("userName", user.getFullName());
            
            // Broadcast to all online users
            messagingTemplate.convertAndSend("/topic/users/online", statusUpdate);
        } catch (Exception e) {
            log.error("Error broadcasting online status: {}", e.getMessage());
        }
    }

    /**
     * Handle offline status updates
     */
    @MessageMapping("/status.offline")
    public void broadcastOfflineStatus(Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElseThrow();
            
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("type", "OFFLINE");
            statusUpdate.put("userId", user.getId());
            
            messagingTemplate.convertAndSend("/topic/users/online", statusUpdate);
        } catch (Exception e) {
            log.error("Error broadcasting offline status: {}", e.getMessage());
        }
    }
}
