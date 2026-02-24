package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.MessageRequest;
import zm.unza.counseling.dto.response.ConversationDto;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.MessageRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public Message sendMessage(Long senderId, MessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setSubject(request.getSubject());
        message.setContent(request.getContent());
        
        // Set conversation ID based on sender and recipient
        long convId = senderId < request.getRecipientId() ? 
            senderId * 1000000 + request.getRecipientId() : 
            request.getRecipientId() * 1000000 + senderId;
        message.setConversationId(convId);
        
        return messageRepository.save(message);
    }

    public List<Message> getReceivedMessages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.findByRecipientOrderBySentAtDesc(user);
    }

    public List<Message> getSentMessages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.findBySenderOrderBySentAtDesc(user);
    }
    
    public List<Message> getAllMessages(Long userId) {
        return messageRepository.findAllMessagesByUserId(userId);
    }

    public Message getMessageById(Long id, Long userId) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify user has access to this message
        if (!message.getSender().getId().equals(userId) && 
            !message.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this message");
        }
        
        return message;
    }

    @Transactional
    public Message updateMessage(Long id, MessageRequest request, Long userId) {
        Message message = getMessageById(id, userId);
        message.setContent(request.getContent());
        return messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Long id, Long userId) {
        Message message = getMessageById(id, userId);
        messageRepository.delete(message);
    }

    public List<Message> getMessagesByConversation(Long conversationId, Long userId) {
        return messageRepository.findByConversationIdOrderBySentAtDesc(conversationId);
    }

    public List<Message> getMessagesByUser(Long userId, Long currentUserId) {
        return messageRepository.findBySenderIdOrRecipientId(userId, currentUserId);
    }

    public List<Message> searchMessages(String query, Long userId) {
        return messageRepository.findByContentContaining(query);
    }

    @Transactional
    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = getMessageById(messageId, userId);
        if (message.getRecipient().getId().equals(userId)) {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }
    }

    @Transactional
    public void markMessageAsDelivered(Long messageId, Long userId) {
        Message message = getMessageById(messageId, userId);
        if (message.getRecipient().getId().equals(userId)) {
            message.setDelivered(true);
            messageRepository.save(message);
        }
    }
    
    @Transactional
    public void markAllMessagesAsRead(Long userId) {
        messageRepository.markAllMessagesAsReadByUser(userId);
    }
    
    @Transactional
    public void markAllMessagesFromPartnerAsRead(Long userId, Long partnerId) {
        messageRepository.markAllMessagesAsRead(userId, partnerId);
    }

    public long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }
    
    public Map<String, Long> getMessageStatistics(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMessages", messageRepository.countTotalMessages(userId));
        stats.put("unreadMessages", messageRepository.countUnreadMessages(userId));
        stats.put("sentMessages", messageRepository.countSentMessages(userId));
        stats.put("receivedMessages", messageRepository.countReceivedMessages(userId));
        return stats;
    }

    /**
     * Get all conversations for a user, grouped by conversation partner
     * Enhanced with partner type, profile picture, and online status
     */
    public List<ConversationDto> getConversations(Long userId) {
        List<Long> partnerIds = messageRepository.findDistinctConversationPartners(userId);
        
        User currentUser = userRepository.findById(userId).orElseThrow();
        boolean isCounselor = currentUser.isCounselor();
        
        List<ConversationDto> conversations = new ArrayList<>();
        
        for (Long partnerId : partnerIds) {
            Optional<User> partnerOpt = userRepository.findById(partnerId);
            if (partnerOpt.isPresent()) {
                User partner = partnerOpt.get();
                
                // Counselor-client relationship check
                if (isCounselor && !partner.isStudent()) {
                    continue; // Counselors should only see client conversations
                }
                if (!isCounselor && partner.isCounselor()) {
                    // Clients can only message counselors they have appointments with
                    if (!hasAppointmentRelationship(userId, partnerId)) {
                        continue;
                    }
                }
                
                Optional<Message> lastMessage = messageRepository.findLastMessageInConversation(userId, partnerId);
                long unreadCount = messageRepository.countUnreadMessagesFromPartner(userId, partnerId);
                
                ConversationDto dto = new ConversationDto();
                dto.setPartnerId(partner.getId());
                dto.setPartnerUsername(partner.getUsername());
                dto.setPartnerEmail(partner.getEmail());
                dto.setPartnerFirstName(partner.getFirstName());
                dto.setPartnerLastName(partner.getLastName());
                dto.setPartnerFullName(partner.getFullName());
                dto.setPartnerProfilePicture(partner.getProfilePicture());
                dto.setPartnerType(partner.isCounselor() ? "COUNSELOR" : "CLIENT");
                dto.setUnreadCount((int) unreadCount);
                
                // Add counselor-specific fields
                if (partner.isCounselor()) {
                    dto.setPartnerSpecialization(partner.getSpecialization());
                }
                
                // Add client-specific fields
                if (partner.isStudent()) {
                    dto.setPartnerStudentId(partner.getStudentId());
                }
                
                if (lastMessage.isPresent()) {
                    Message msg = lastMessage.get();
                    dto.setLastMessageContent(msg.getContent());
                    dto.setLastMessageTime(msg.getSentAt());
                    dto.setConversationId(userId < partnerId ? 
                        userId * 1000000 + partnerId : 
                        partnerId * 1000000 + userId);
                }
                
                conversations.add(dto);
            }
        }
        
        return conversations.stream()
            .sorted((a, b) -> {
                if (a.getLastMessageTime() == null) return 1;
                if (b.getLastMessageTime() == null) return -1;
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Check if user has appointment relationship with partner
     */
    private boolean hasAppointmentRelationship(Long userId, Long partnerId) {
        User student = userRepository.findById(userId).orElseThrow();
        List<Appointment> appointments = appointmentRepository.findByStudent(student);
        return appointments.stream()
            .anyMatch(a -> a.getCounselor() != null && a.getCounselor().getId().equals(partnerId));
    }

    /**
     * Get messages exchanged with a specific conversation partner
     */
    public List<Message> getConversationWithPartner(Long userId, Long partnerId) {
        return messageRepository.findConversationMessages(userId, partnerId);
    }

    /**
     * Reply to a message
     */
    @Transactional
    public Message replyToMessage(Long messageId, String content, Long senderId) {
        Message originalMessage = getMessageById(messageId, senderId);
        User sender = userRepository.findById(senderId).orElseThrow();
        User recipient = originalMessage.getSender().getId().equals(senderId) ? 
            originalMessage.getRecipient() : originalMessage.getSender();
        
        Message reply = new Message();
        reply.setSender(sender);
        reply.setRecipient(recipient);
        reply.setSubject("Re: " + originalMessage.getSubject());
        reply.setContent(content);
        
        // Use same conversation ID as original message
        reply.setConversationId(originalMessage.getConversationId());
        
        return messageRepository.save(reply);
    }
    
    /**
     * Forward a message to recipients
     */
    @Transactional
    public List<Message> forwardMessage(Long messageId, List<Number> recipientIds, String additionalContent, Long senderId) {
        Message originalMessage = getMessageById(messageId, senderId);
        User sender = userRepository.findById(senderId).orElseThrow();
        
        List<Message> forwardedMessages = new ArrayList<>();
        
        for (Number recipientId : recipientIds) {
            User recipient = userRepository.findById(recipientId.longValue()).orElseThrow();
            
            Message forwarded = new Message();
            forwarded.setSender(sender);
            forwarded.setRecipient(recipient);
            forwarded.setSubject("Fwd: " + originalMessage.getSubject());
            
            String fullContent = additionalContent != null ? 
                additionalContent + "\\n\\n--- Forwarded Message ---\\n" + originalMessage.getContent() :
                "--- Forwarded Message ---\\n" + originalMessage.getContent();
            forwarded.setContent(fullContent);
            
            forwardedMessages.add(messageRepository.save(forwarded));
        }
        
        return forwardedMessages;
    }

    /**
     * Archive a message
     */
    @Transactional
    public void archiveMessage(Long messageId, Long userId) {
        messageRepository.archiveMessage(messageId, userId);
    }
    
    /**
     * Unarchive a message
     */
    @Transactional
    public void unarchiveMessage(Long messageId, Long userId) {
        messageRepository.unarchiveMessage(messageId, userId);
    }

    /**
     * Get archived messages
     */
    public List<Message> getArchivedMessages(Long userId) {
        return messageRepository.findArchivedMessages(userId);
    }

    /**
     * Star a message
     */
    @Transactional
    public void starMessage(Long messageId, Long userId) {
        messageRepository.starMessage(messageId, userId);
    }
    
    /**
     * Unstar a message
     */
    @Transactional
    public void unstarMessage(Long messageId, Long userId) {
        messageRepository.unstarMessage(messageId, userId);
    }

    /**
     * Get starred messages
     */
    public List<Message> getStarredMessages(Long userId) {
        return messageRepository.findStarredMessages(userId);
    }

    /**
     * Bulk delete messages
     */
    @Transactional
    public void bulkDeleteMessages(List<String> messageIds, Long userId) {
        for (String id : messageIds) {
            deleteMessage(Long.parseLong(id), userId);
        }
    }
    
    /**
     * Bulk mark messages as read
     */
    @Transactional
    public void bulkMarkAsRead(List<String> messageIds, Long userId) {
        List<Long> ids = messageIds.stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
        messageRepository.bulkMarkAsRead(ids, userId);
    }
}
