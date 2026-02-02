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
@RequestMapping({"/notifications", "/v1/notifications"})
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
        // Get the current user's ID
        Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}