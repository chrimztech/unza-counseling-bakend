package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.MessageRequest;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.service.MessageService;
import zm.unza.counseling.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.sendMessage(sender.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getReceivedMessages(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessageById(id, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable Long id, @RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.updateMessage(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.deleteMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getMessagesByConversation(@PathVariable Long conversationId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessagesByConversation(conversationId, user.getId()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Message>> getMessagesByUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessagesByUser(userId, user.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Message>> searchMessages(@RequestParam String query, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.searchMessages(query, user.getId()));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.markMessageAsRead(messageId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{messageId}/delivered")
    public ResponseEntity<Void> markMessageAsDelivered(@PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.markMessageAsDelivered(messageId, user.getId());
        return ResponseEntity.ok().build();
    }
}
