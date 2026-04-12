package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.MessageRequest;
import zm.unza.counseling.dto.response.MessageAuditDto;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.MessageService;
import zm.unza.counseling.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/v1/messages", "/messages"})
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;

    // ============ MESSAGE ENDPOINTS ============
    
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.sendMessage(sender.getId(), request));
    }

    @PostMapping
    public ResponseEntity<Message> sendMessageV2(@RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.sendMessage(sender.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getAllMessages(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessageById(id, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable Long id, @RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.updateMessage(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.deleteMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getMessagesByConversation(@PathVariable Long conversationId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessagesByConversation(conversationId, user.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Message>> searchMessages(@RequestParam String query, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.searchMessages(query, user.getId()));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.markMessageAsRead(messageId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{messageId}/delivered")
    public ResponseEntity<Void> markMessageAsDelivered(@PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.markMessageAsDelivered(messageId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.markAllMessagesAsRead(user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        long count = messageService.getUnreadCount(user.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getMessageStatistics(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessageStatistics(user.getId()));
    }

    @PostMapping("/{messageId}/reply")
    public ResponseEntity<Message> replyToMessage(
            @PathVariable Long messageId, 
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.replyToMessage(messageId, request.get("content"), sender.getId()));
    }

    @PostMapping("/{messageId}/forward")
    public ResponseEntity<List<Message>> forwardMessage(
            @PathVariable Long messageId, 
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        @SuppressWarnings("unchecked")
        List<?> recipientIds = (List<?>) request.get("recipientIds");
        String additionalContent = (String) request.get("additionalContent");
        return ResponseEntity.ok(messageService.forwardMessage(messageId, recipientIds, additionalContent, sender.getId()));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archiveMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.archiveMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<Void> unarchiveMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.unarchiveMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/archived")
    public ResponseEntity<List<Message>> getArchivedMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getArchivedMessages(user.getId()));
    }

    @PutMapping("/{id}/star")
    public ResponseEntity<Void> starMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.starMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unstar")
    public ResponseEntity<Void> unstarMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.unstarMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/starred")
    public ResponseEntity<List<Message>> getStarredMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getStarredMessages(user.getId()));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDeleteMessages(@RequestBody Map<String, List<String>> request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.bulkDeleteMessages(request.get("messageIds"), user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-read")
    public ResponseEntity<Void> bulkMarkAsRead(@RequestBody Map<String, List<String>> request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.bulkMarkAsRead(request.get("messageIds"), user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MessageAuditDto>>> getMessageAuditRecords(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getMessagesForAudit(query, senderId, recipientId, start, end)
        ));
    }

    @GetMapping("/audit/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMessageAuditStats() {
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessageAuditStats()));
    }
}
