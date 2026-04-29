package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.CompletableFuture;
import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
import zm.unza.counseling.entity.Notification;
import zm.unza.counseling.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;

    /**
     * Send system notification
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Notification> sendSystemNotification(Long userId, String title, 
                                                                String message, String priority) {
        return sendNotification(userId, title, message, "SYSTEM", priority, "/dashboard");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Notification> sendNotification(
            Long userId,
            String title,
            String message,
            String type,
            String priority,
            String actionUrl
    ) {
        if (userId == null) {
            return CompletableFuture.completedFuture(null);
        }

        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : "SYSTEM");
        notification.setPriority(priority);
        notification.setActionUrl(actionUrl);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return CompletableFuture.completedFuture(notificationRepository.saveAndFlush(notification));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotifications(
            Collection<Long> userIds,
            String title,
            String message,
            String type,
            String priority,
            String actionUrl
    ) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Notification> notifications = userIds.stream()
                .filter(userId -> userId != null)
                .distinct()
                .map(userId -> {
                    Notification notification = new Notification();
                    notification.setRecipientId(userId);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setType(type != null ? type : "SYSTEM");
                    notification.setPriority(priority);
                    notification.setActionUrl(actionUrl);
                    notification.setIsRead(false);
                    notification.setCreatedAt(now);
                    return notification;
                })
                .toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAllAndFlush(notifications);
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long id, Long userId) {
        Notification notification = getOwnedNotification(id, userId);

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    public int markAllAsRead(Long userId) {
        List<Notification> unreadNotifications =
                notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false);

        if (unreadNotifications.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        notificationRepository.saveAll(unreadNotifications);
        return unreadNotifications.size();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsRead(userId, false);
    }

    public void deleteNotification(Long id, Long userId) {
        Notification notification = getOwnedNotification(id, userId);
        notificationRepository.delete(notification);
    }

    private Notification getOwnedNotification(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getRecipientId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this notification");
        }

        return notification;
    }
}
