package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.request.MessageRequest;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.MessageRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Message sendMessage(Long senderId, MessageRequest request) {
        User sender = userRepository.findById(senderId).orElseThrow();
        User recipient = userRepository.findById(request.getRecipientId()).orElseThrow();

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setSubject(request.getSubject());
        message.setContent(request.getContent());
        
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

    public Message getMessageById(Long id, Long userId) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public Message updateMessage(Long id, MessageRequest request, Long userId) {
        Message message = getMessageById(id, userId);
        message.setContent(request.getContent());
        return messageRepository.save(message);
    }

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

    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = getMessageById(messageId, userId);
        message.setRead(true);
        messageRepository.save(message);
    }

    public void markMessageAsDelivered(Long messageId, Long userId) {
        Message message = getMessageById(messageId, userId);
        message.setDelivered(true);
        messageRepository.save(message);
    }
}
