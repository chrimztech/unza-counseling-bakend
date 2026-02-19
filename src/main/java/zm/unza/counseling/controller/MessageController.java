package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.MessageRequest;
import zm.unza.counseling.dto.response.ConversationDto;
import zm.unza.counseling.dto.response.UserResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.MessageService;
import zm.unza.counseling.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1", "/v1"})
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    // ============ MESSAGE ENDPOINTS ============
    
    @PostMapping("/messages/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.sendMessage(sender.getId(), request));
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessageV2(@RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.sendMessage(sender.getId(), request));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getReceivedMessages(user.getId()));
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessageById(id, user.getId()));
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable Long id, @RequestBody MessageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.updateMessage(id, request, user.getId()));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.deleteMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getMessagesByConversation(@PathVariable Long conversationId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getMessagesByConversation(conversationId, null));
    }

    @GetMapping("/messages/search")
    public ResponseEntity<List<Message>> searchMessages(@RequestParam String query, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.searchMessages(query, user.getId()));
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.markMessageAsRead(messageId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{messageId}/delivered")
    public ResponseEntity<Void> markMessageAsDelivered(@PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.markMessageAsDelivered(messageId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.markAllMessagesAsRead(user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        long count = messageService.getUnreadCount(user.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/statistics")
    public ResponseEntity<Map<String, Long>> getMessageStatistics(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getMessageStatistics(user.getId()));
    }

    @PostMapping("/messages/{messageId}/reply")
    public ResponseEntity<Message> replyToMessage(
            @PathVariable Long messageId, 
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.replyToMessage(messageId, request.get("content"), sender.getId()));
    }

    @PostMapping("/messages/{messageId}/forward")
    public ResponseEntity<List<Message>> forwardMessage(
            @PathVariable Long messageId, 
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User sender = userService.getUserByEmail(userDetails.getUsername());
        @SuppressWarnings("unchecked")
        List<Number> recipientIds = (List<Number>) request.get("recipientIds");
        String additionalContent = (String) request.get("additionalContent");
        return ResponseEntity.ok(messageService.forwardMessage(messageId, recipientIds, additionalContent, sender.getId()));
    }

    @PutMapping("/messages/{id}/archive")
    public ResponseEntity<Void> archiveMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.archiveMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{id}/unarchive")
    public ResponseEntity<Void> unarchiveMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.unarchiveMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/archived")
    public ResponseEntity<List<Message>> getArchivedMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getArchivedMessages(user.getId()));
    }

    @PutMapping("/messages/{id}/star")
    public ResponseEntity<Void> starMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.starMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{id}/unstar")
    public ResponseEntity<Void> unstarMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.unstarMessage(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/starred")
    public ResponseEntity<List<Message>> getStarredMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getStarredMessages(user.getId()));
    }

    @PostMapping("/messages/bulk-delete")
    public ResponseEntity<Void> bulkDeleteMessages(@RequestBody Map<String, List<String>> request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.bulkDeleteMessages(request.get("messageIds"), user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/bulk-read")
    public ResponseEntity<Void> bulkMarkAsRead(@RequestBody Map<String, List<String>> request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.bulkMarkAsRead(request.get("messageIds"), user.getId());
        return ResponseEntity.ok().build();
    }

    // ============ CONVERSATION ENDPOINTS ============

    @GetMapping("/conversations/user/{userId}")
    public ResponseEntity<List<ConversationDto>> getConversationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getConversations(userId));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getConversations(user.getId()));
    }

    @GetMapping("/conversations/{partnerId}")
    public ResponseEntity<List<Message>> getConversationWithPartner(
            @PathVariable Long partnerId, 
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getConversationWithPartner(user.getId(), partnerId));
    }

    @PutMapping("/conversations/{partnerId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long partnerId, 
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        messageService.markAllMessagesFromPartnerAsRead(user.getId(), partnerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/threads")
    public ResponseEntity<List<Message>> getMessageThreads(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getAllMessages(user.getId()));
    }
    
    // ============ CONTACTS ENDPOINTS ============
    
    /**
     * Get available contacts for messaging
     * Counselors see their assigned clients
     * Clients see their assigned counselors
     */
    @GetMapping("/contacts/available")
    public ResponseEntity<List<UserResponse>> getAvailableContacts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        List<User> contacts;
        
        if (user.isCounselor()) {
            // Counselors see clients with appointments
            contacts = appointmentRepository.findByCounselor(user).stream()
                .map(Appointment::getClient)
                .filter(c -> c != null)
                .map(client -> (User) client)
                .distinct()
                .collect(Collectors.toList());
        } else {
            // Clients see their assigned counselor
            contacts = appointmentRepository.findByStudent(user).stream()
                .map(Appointment::getCounselor)
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(contacts.stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList()));
    }
    
    /**
     * Search contacts for messaging
     */
    @GetMapping("/contacts/search")
    public ResponseEntity<List<UserResponse>> searchContacts(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        List<User> contacts = getAvailableContactsList(user.getId());
        
        String lowerQuery = query.toLowerCase();
        List<User> filtered = contacts.stream()
            .filter(c -> c.getFullName().toLowerCase().contains(lowerQuery) ||
                        c.getEmail().toLowerCase().contains(lowerQuery) ||
                        (c.getStudentId() != null && c.getStudentId().toLowerCase().contains(lowerQuery)))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(filtered.stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList()));
    }
    
    /**
     * Get list of users user can message
     */
    private List<User> getAvailableContactsList(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.isCounselor()) {
            return appointmentRepository.findByCounselor(user).stream()
                .map(Appointment::getClient)
                .filter(c -> c != null)
                .map(client -> (User) client)
                .distinct()
                .collect(Collectors.toList());
        } else {
            return appointmentRepository.findByStudent(user).stream()
                .map(Appointment::getCounselor)
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setActive(user.getActive());
        return response;
    }
}
