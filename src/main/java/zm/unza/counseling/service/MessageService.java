package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.MessageRequest;
import zm.unza.counseling.dto.response.ConversationDto;
import zm.unza.counseling.dto.response.MessageAuditDto;
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
    private final AuditLogService auditLogService;

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
        message.setConversationId(buildConversationId(senderId, request.getRecipientId()));

        Message saved = messageRepository.save(message);
        auditMessageAction("MESSAGE_SENT", saved, senderId, buildAuditDetails(saved));
        return saved;
    }

    public List<Message> getReceivedMessages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.findByRecipientOrderBySentAtDesc(user).stream()
                .filter(message -> !message.isDeletedByRecipient())
                .collect(Collectors.toList());
    }

    public List<Message> getSentMessages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.findBySenderOrderBySentAtDesc(user).stream()
                .filter(message -> !message.isDeletedBySender())
                .collect(Collectors.toList());
    }
    
    public List<Message> getAllMessages(Long userId) {
        return messageRepository.findAllMessagesByUserId(userId).stream()
                .filter(message -> !isDeletedForUser(message, userId))
                .sorted((a, b) -> b.getSentAt().compareTo(a.getSentAt()))
                .collect(Collectors.toList());
    }

    public Message getMessageById(Long id, Long userId) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify user has access to this message
        if (!message.getSender().getId().equals(userId) && 
            !message.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this message");
        }

        if (isDeletedForUser(message, userId)) {
            throw new RuntimeException("Message not found");
        }
        
        return message;
    }

    @Transactional
    public Message updateMessage(Long id, MessageRequest request, Long userId) {
        Message message = getMessageById(id, userId);
        message.setContent(request.getContent());
        Message saved = messageRepository.save(message);
        auditMessageAction("MESSAGE_UPDATED", saved, userId, buildAuditDetails(saved));
        return saved;
    }

    @Transactional
    public void deleteMessage(Long id, Long userId) {
        Message message = getMessageById(id, userId);
        if (message.getSender().getId().equals(userId)) {
            message.setDeletedBySender(true);
        }
        if (message.getRecipient().getId().equals(userId)) {
            message.setDeletedByRecipient(true);
        }
        if (message.getDeletedAt() == null) {
            message.setDeletedAt(LocalDateTime.now());
        }
        messageRepository.save(message);
        auditMessageAction("MESSAGE_DELETED", message, userId, buildAuditDetails(message));
    }

    public List<Message> getMessagesByConversation(Long conversationId, Long userId) {
        List<Message> messages = userId == null
                ? messageRepository.findByConversationIdOrderBySentAtDesc(conversationId)
                : messageRepository.findByConversationIdAndUserAccess(conversationId, userId);

        if (userId == null) {
            return messages;
        }

        return messages.stream()
                .filter(message -> !isDeletedForUser(message, userId))
                .collect(Collectors.toList());
    }

    public List<Message> getMessagesByUser(Long userId, Long currentUserId) {
        return messageRepository.findBySenderIdOrRecipientId(userId, currentUserId);
    }

    public List<Message> searchMessages(String query, Long userId) {
        return messageRepository.searchUserMessages(query, userId).stream()
                .filter(message -> !isDeletedForUser(message, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = getMessageById(messageId, userId);
        if (message.getRecipient().getId().equals(userId)) {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
            auditMessageAction("MESSAGE_READ", message, userId, buildAuditDetails(message));
        }
    }

    @Transactional
    public void markMessageAsDelivered(Long messageId, Long userId) {
        Message message = getMessageById(messageId, userId);
        if (message.getRecipient().getId().equals(userId)) {
            message.setDelivered(true);
            messageRepository.save(message);
            auditMessageAction("MESSAGE_DELIVERED", message, userId, buildAuditDetails(message));
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
        return getAllMessages(userId).stream()
                .filter(message -> message.getRecipient().getId().equals(userId) && !message.isRead())
                .count();
    }
    
    public Map<String, Long> getMessageStatistics(Long userId) {
        List<Message> visibleMessages = getAllMessages(userId);
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMessages", (long) visibleMessages.size());
        stats.put("unreadMessages", visibleMessages.stream()
                .filter(message -> message.getRecipient().getId().equals(userId) && !message.isRead())
                .count());
        stats.put("sentMessages", visibleMessages.stream()
                .filter(message -> message.getSender().getId().equals(userId))
                .count());
        stats.put("receivedMessages", visibleMessages.stream()
                .filter(message -> message.getRecipient().getId().equals(userId))
                .count());
        return stats;
    }

    /**
     * Get all conversations for a user, grouped by conversation partner
     * Enhanced with partner type, profile picture, and online status
     */
    public List<ConversationDto> getConversations(Long userId) {
        User currentUser = userRepository.findById(userId).orElseThrow();
        boolean isCounselor = currentUser.isCounselor();
        List<Message> visibleMessages = getAllMessages(userId);
        Map<Long, List<Message>> messagesByPartner = new LinkedHashMap<>();

        for (Message message : visibleMessages) {
            Long partnerId = message.getSender().getId().equals(userId)
                    ? message.getRecipient().getId()
                    : message.getSender().getId();
            messagesByPartner.computeIfAbsent(partnerId, ignored -> new ArrayList<>()).add(message);
        }
        
        List<ConversationDto> conversations = new ArrayList<>();
        
        for (Map.Entry<Long, List<Message>> entry : messagesByPartner.entrySet()) {
            Long partnerId = entry.getKey();
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
                
                List<Message> conversationMessages = entry.getValue();
                Message lastMessage = conversationMessages.isEmpty() ? null : conversationMessages.get(0);
                long unreadCount = conversationMessages.stream()
                        .filter(message -> message.getRecipient().getId().equals(userId) && !message.isRead())
                        .count();
                
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
                    dto.setPartnerProgramme(partner.getProgram());
                }
                
                if (lastMessage != null) {
                    Message msg = lastMessage;
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
        return messageRepository.findConversationMessages(userId, partnerId).stream()
                .filter(message -> !isDeletedForUser(message, userId))
                .collect(Collectors.toList());
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

        Message saved = messageRepository.save(reply);
        auditMessageAction("MESSAGE_REPLY", saved, senderId, buildAuditDetails(saved));
        return saved;
    }
    
    /**
     * Forward a message to recipients
     */
    @Transactional
    public List<Message> forwardMessage(Long messageId, List<?> recipientIds, String additionalContent, Long senderId) {
        Message originalMessage = getMessageById(messageId, senderId);
        User sender = userRepository.findById(senderId).orElseThrow();
        List<Long> normalizedRecipientIds = normalizeRecipientIds(recipientIds);
        
        List<Message> forwardedMessages = new ArrayList<>();
        
        for (Long recipientId : normalizedRecipientIds) {
            User recipient = userRepository.findById(recipientId).orElseThrow();
            
            Message forwarded = new Message();
            forwarded.setSender(sender);
            forwarded.setRecipient(recipient);
            forwarded.setSubject("Fwd: " + originalMessage.getSubject());
            forwarded.setConversationId(buildConversationId(senderId, recipientId));
            
            String fullContent = additionalContent != null && !additionalContent.isBlank()
                    ? additionalContent + "\n\n--- Forwarded Message ---\n" + originalMessage.getContent()
                    : "--- Forwarded Message ---\n" + originalMessage.getContent();
            forwarded.setContent(fullContent);
            
            Message saved = messageRepository.save(forwarded);
            auditMessageAction("MESSAGE_FORWARDED", saved, senderId, buildAuditDetails(saved));
            forwardedMessages.add(saved);
        }
        
        return forwardedMessages;
    }

    /**
     * Archive a message
     */
    @Transactional
    public void archiveMessage(Long messageId, Long userId) {
        messageRepository.archiveMessage(messageId, userId);
        messageRepository.findById(messageId).ifPresent(message ->
                auditMessageAction("MESSAGE_ARCHIVED", message, userId, buildAuditDetails(message)));
    }
    
    /**
     * Unarchive a message
     */
    @Transactional
    public void unarchiveMessage(Long messageId, Long userId) {
        messageRepository.unarchiveMessage(messageId, userId);
        messageRepository.findById(messageId).ifPresent(message ->
                auditMessageAction("MESSAGE_UNARCHIVED", message, userId, buildAuditDetails(message)));
    }

    /**
     * Get archived messages
     */
    public List<Message> getArchivedMessages(Long userId) {
        return messageRepository.findArchivedMessages(userId).stream()
                .filter(message -> !isDeletedForUser(message, userId))
                .collect(Collectors.toList());
    }

    /**
     * Star a message
     */
    @Transactional
    public void starMessage(Long messageId, Long userId) {
        messageRepository.starMessage(messageId, userId);
        messageRepository.findById(messageId).ifPresent(message ->
                auditMessageAction("MESSAGE_STARRED", message, userId, buildAuditDetails(message)));
    }
    
    /**
     * Unstar a message
     */
    @Transactional
    public void unstarMessage(Long messageId, Long userId) {
        messageRepository.unstarMessage(messageId, userId);
        messageRepository.findById(messageId).ifPresent(message ->
                auditMessageAction("MESSAGE_UNSTARRED", message, userId, buildAuditDetails(message)));
    }

    /**
     * Get starred messages
     */
    public List<Message> getStarredMessages(Long userId) {
        return messageRepository.findStarredMessages(userId).stream()
                .filter(message -> !isDeletedForUser(message, userId))
                .collect(Collectors.toList());
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

    public List<MessageAuditDto> getMessagesForAudit(String query, Long senderId, Long recipientId, LocalDateTime start, LocalDateTime end) {
        return messageRepository.findAll(Sort.by(Sort.Direction.DESC, "sentAt")).stream()
                .filter(message -> senderId == null || message.getSender().getId().equals(senderId))
                .filter(message -> recipientId == null || message.getRecipient().getId().equals(recipientId))
                .filter(message -> start == null || (message.getSentAt() != null && !message.getSentAt().isBefore(start)))
                .filter(message -> end == null || (message.getSentAt() != null && !message.getSentAt().isAfter(end)))
                .filter(message -> {
                    if (query == null || query.isBlank()) {
                        return true;
                    }
                    String normalized = query.toLowerCase();
                    return String.valueOf(message.getSubject()).toLowerCase().contains(normalized)
                            || String.valueOf(message.getContent()).toLowerCase().contains(normalized)
                            || message.getSender().getFullName().toLowerCase().contains(normalized)
                            || message.getSender().getEmail().toLowerCase().contains(normalized)
                            || message.getRecipient().getFullName().toLowerCase().contains(normalized)
                            || message.getRecipient().getEmail().toLowerCase().contains(normalized);
                })
                .map(this::toAuditDto)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getMessageAuditStats() {
        List<Message> messages = messageRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMessages", (long) messages.size());
        stats.put("deletedMessages", messages.stream()
                .filter(message -> message.isDeletedBySender() || message.isDeletedByRecipient())
                .count());
        stats.put("readMessages", messages.stream().filter(Message::isRead).count());
        stats.put("unreadMessages", messages.stream().filter(message -> !message.isRead()).count());
        stats.put("conversationCount", messages.stream()
                .map(Message::getConversationId)
                .filter(Objects::nonNull)
                .distinct()
                .count());
        return stats;
    }

    private MessageAuditDto toAuditDto(Message message) {
        MessageAuditDto dto = new MessageAuditDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setSenderEmail(message.getSender().getEmail());
        dto.setRecipientId(message.getRecipient().getId());
        dto.setRecipientName(message.getRecipient().getFullName());
        dto.setRecipientEmail(message.getRecipient().getEmail());
        dto.setSubject(message.getSubject());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setReadAt(message.getReadAt());
        dto.setDeletedAt(message.getDeletedAt());
        dto.setRead(message.isRead());
        dto.setDelivered(message.isDelivered());
        dto.setArchived(message.isArchived());
        dto.setStarred(message.isStarred());
        dto.setDeletedBySender(message.isDeletedBySender());
        dto.setDeletedByRecipient(message.isDeletedByRecipient());
        return dto;
    }

    private boolean isDeletedForUser(Message message, Long userId) {
        if (message.getSender() != null && message.getSender().getId().equals(userId)) {
            return message.isDeletedBySender();
        }
        if (message.getRecipient() != null && message.getRecipient().getId().equals(userId)) {
            return message.isDeletedByRecipient();
        }
        return false;
    }

    private long buildConversationId(Long firstUserId, Long secondUserId) {
        long smallerId = Math.min(firstUserId, secondUserId);
        long largerId = Math.max(firstUserId, secondUserId);
        return smallerId * 1000000 + largerId;
    }

    private List<Long> normalizeRecipientIds(List<?> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            throw new IllegalArgumentException("recipientIds must not be empty");
        }

        return recipientIds.stream()
                .map(this::toRecipientId)
                .distinct()
                .collect(Collectors.toList());
    }

    private Long toRecipientId(Object rawRecipientId) {
        if (rawRecipientId instanceof Number number) {
            return number.longValue();
        }
        if (rawRecipientId instanceof String value && !value.isBlank()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid recipientId: " + value, ex);
            }
        }
        throw new IllegalArgumentException("Invalid recipientId: " + rawRecipientId);
    }

    private void auditMessageAction(String action, Message message, Long userId, String details) {
        auditLogService.logAction(
                action,
                "MESSAGE",
                String.valueOf(message.getId()),
                details,
                String.valueOf(userId),
                null,
                true
        );
    }

    private String buildAuditDetails(Message message) {
        String contentPreview = message.getContent() == null
                ? ""
                : message.getContent().substring(0, Math.min(message.getContent().length(), 160));
        return "subject=" + String.valueOf(message.getSubject())
                + ", sender=" + message.getSender().getEmail()
                + ", recipient=" + message.getRecipient().getEmail()
                + ", content=" + contentPreview;
    }
}
