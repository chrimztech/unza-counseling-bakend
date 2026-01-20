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
}
