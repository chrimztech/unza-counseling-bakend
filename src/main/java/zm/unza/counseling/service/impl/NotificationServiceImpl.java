package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import zm.unza.counseling.entity.Notification;
import zm.unza.counseling.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailServiceImpl emailService;

    /**
     * Create and send a notification
     */
    public CompletableFuture<Notification> createNotification(String recipientId, String title, 
                                                             String message, String type, 
                                                             String priority, String actionUrl) {
        Notification notification = new Notification();
        // notification.setRecipientId(recipientId);
        // notification.setTitle(title);
        // notification.setMessage(message);
        // notification.setType(type);
        // notification.setPriority(priority);
        // notification.setActionUrl(actionUrl);
        // notification.setRead(false);
        // notification.setCreatedAt(LocalDateTime.now());
        
        notification = notificationRepository.save(notification);
        
        // Send real-time notification via WebSocket
        sendRealtimeNotification(recipientId, notification);
        
        log.info("Notification created and sent to user: {}", recipientId);
        return CompletableFuture.completedFuture(notification);
    }

    /**
     * Send appointment notification
     */
    public CompletableFuture<Void> sendAppointmentNotification(String userId, String appointmentId, 
                                                             String type, String counselorName, 
                                                             String appointmentDate, String userEmail) {
        String title = "Appointment " + type;
        String message = String.format("Your appointment with %s on %s has been %s.", 
                                     counselorName, appointmentDate, type.toLowerCase());
        
        return createNotification(userId, title, message, "APPOINTMENT", "MEDIUM", 
                                "/appointments/" + appointmentId)
            .thenCompose(notification -> {
                // Send email notification
                if ("CONFIRMED".equals(type)) {
                    return emailService.sendAppointmentConfirmation(userEmail, 
                                                                   "User", // Replace with actual name
                                                                   counselorName, 
                                                                   appointmentDate, 
                                                                   "TBD"); // Replace with actual time
                } else if ("REMINDER".equals(type)) {
                    return emailService.sendAppointmentReminder(userEmail, 
                                                               "User", // Replace with actual name
                                                               counselorName, 
                                                               appointmentDate, 
                                                               "TBD"); // Replace with actual time
                }
                return CompletableFuture.completedFuture(null);
            });
    }

    /**
     * Send risk assessment notification
     */
    public CompletableFuture<Void> sendRiskAssessmentNotification(String counselorId, String clientName, 
                                                                String riskLevel, String assessmentDate) {
        String title = "Risk Assessment Alert";
        String message = String.format("Client %s has been assessed with %s risk level on %s", 
                                     clientName, riskLevel, assessmentDate);
        
        return createNotification(counselorId, title, message, "RISK_ASSESSMENT", 
                                getRiskPriority(riskLevel), "/risk-assessments")
            .thenCompose(notification -> {
                // Send email to counselor
                return emailService.sendRiskAssessmentAlert("counselor@unza.zm", // Replace with actual email
                                                          clientName, 
                                                          riskLevel, 
                                                          assessmentDate);
            });
    }

    /**
     * Send system notification
     */
    public CompletableFuture<Notification> sendSystemNotification(String userId, String title, 
                                                                String message, String priority) {
        return createNotification(userId, title, message, "SYSTEM", priority, "/dashboard");
    }

    /**
     * Mark notification as read
     */
    public CompletableFuture<Notification> markAsRead(String notificationId) {
        return CompletableFuture.supplyAsync(() -> {
            Notification notification = notificationRepository.findById(Long.parseLong(notificationId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));
            
            // notification.setRead(true);
            // notification.setReadAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        });
    }

    /**
     * Get unread notifications for a user
     */
    public CompletableFuture<List<Notification>> getUnreadNotifications(String userId) {
        return CompletableFuture.supplyAsync(() -> 
            notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false)
        );
    }

    /**
     * Get all notifications for a user
     */
    public CompletableFuture<List<Notification>> getAllNotifications(String userId) {
        return CompletableFuture.supplyAsync(() -> 
            notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
        );
    }

    /**
     * Send real-time notification via WebSocket
     */
    private void sendRealtimeNotification(String userId, Notification notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to user: {}", userId, e);
        }
    }

    /**
     * Get priority based on risk level
     */
    private String getRiskPriority(String riskLevel) {
        switch (riskLevel.toUpperCase()) {
            case "HIGH":
                return "HIGH";
            case "MEDIUM":
                return "MEDIUM";
            case "LOW":
                return "LOW";
            default:
                return "MEDIUM";
        }
    }

    /**
     * Bulk create notifications
     */
    public CompletableFuture<List<Notification>> createBulkNotifications(List<String> userIds, 
                                                                       String title, String message, 
                                                                       String type, String priority) {
        return CompletableFuture.supplyAsync(() -> {
            List<Notification> notifications = userIds.stream().map(userId -> {
                Notification notification = new Notification();
                // notification.setRecipientId(userId);
                // notification.setTitle(title);
                // notification.setMessage(message);
                // notification.setType(type);
                // notification.setPriority(priority);
                // notification.setRead(false);
                // notification.setCreatedAt(LocalDateTime.now());
                return notification;
            }).toList();

            List<Notification> saved = notificationRepository.saveAll(notifications);
            
            // Send real-time notifications
            // saved.forEach(notification -> 
            //     sendRealtimeNotification(notification.getRecipientId(), notification)
            // );
            
            return saved;
        });
    }
}