package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import zm.unza.counseling.entity.Notification;
import zm.unza.counseling.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;

    /**
     * Send system notification
     */
    public CompletableFuture<Notification> sendSystemNotification(Long userId, String title, 
                                                                String message, String priority) {
        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("SYSTEM");
        notification.setPriority(priority);
        notification.setIsRead(false);
        
        return CompletableFuture.completedFuture(notificationRepository.save(notification));
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
