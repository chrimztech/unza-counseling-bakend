package zm.unza.counseling.service;

import java.util.concurrent.CompletableFuture;
import zm.unza.counseling.entity.Notification;

public class NotificationService {
    
    /**
     * Send system notification
     */
    public CompletableFuture<Notification> sendSystemNotification(String userId, String title, 
                                                                String message, String priority) {
        // Mock implementation - replace with actual logic
        return CompletableFuture.completedFuture(null);
    }
}
