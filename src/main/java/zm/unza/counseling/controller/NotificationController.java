package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.entity.Notification;
import zm.unza.counseling.service.NotificationService;
import zm.unza.counseling.service.UserService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/notifications", "/api/notifications", "/v1/notifications", "/notifications"})
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userIdLong;
        
        // Handle special cases where we should use the authenticated user's ID
        if (userId.equals("admin-user-id") || userId.equals("current-user") || 
            userId.equals("me") || userId.equals("user-id") || !isNumeric(userId)) {
            // Use the authenticated user's ID
            userIdLong = userService.getUserByEmail(userDetails.getUsername()).getId();
        } else {
            userIdLong = Long.parseLong(userId);
        }
        
        return ResponseEntity.ok(notificationService.getUserNotifications(userIdLong));
    }

    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @GetMapping("/user/{userId}/notifications")
    public ResponseEntity<List<Notification>> getUserNotificationsByUserId(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userIdLong;
        
        // Handle special cases where we should use the authenticated user's ID
        if (userId.equals("admin-user-id") || userId.equals("current-user") || 
            userId.equals("me") || userId.equals("user-id") || !isNumeric(userId)) {
            // Use the authenticated user's ID
            userIdLong = userService.getUserByEmail(userDetails.getUsername()).getId();
        } else {
            userIdLong = Long.parseLong(userId);
        }
        
        return ResponseEntity.ok(notificationService.getUserNotifications(userIdLong));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getCurrentUserNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUserNotifications(resolveCurrentUserId(userDetails)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id, resolveCurrentUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    @PutMapping({"/read-all", "/user/{userId}/mark-all-read"})
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable(required = false) String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(resolveUserId(userId, userDetails));
        return ResponseEntity.ok().build();
    }

    @GetMapping({"/unread-count", "/user/{userId}/unread-count"})
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable(required = false) String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUnreadCount(resolveUserId(userId, userDetails)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.deleteNotification(id, resolveCurrentUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    private Long resolveCurrentUserId(UserDetails userDetails) {
        return userService.getUserByEmail(userDetails.getUsername()).getId();
    }

    private Long resolveUserId(String userId, UserDetails userDetails) {
        if (userId == null || userId.isBlank()) {
            return resolveCurrentUserId(userDetails);
        }

        if (userId.equals("admin-user-id") || userId.equals("current-user") ||
            userId.equals("me") || userId.equals("user-id") || !isNumeric(userId)) {
            return resolveCurrentUserId(userDetails);
        }

        return Long.parseLong(userId);
    }
}
