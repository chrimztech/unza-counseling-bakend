package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ConversationDto;
import zm.unza.counseling.dto.response.UserResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.MessageService;
import zm.unza.counseling.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/v1/conversations", "/conversations"})
@RequiredArgsConstructor
public class ConversationsController {

    private final MessageService messageService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationDto>> getConversationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getConversations(userId));
    }

    @GetMapping
    public ResponseEntity<List<ConversationDto>> getConversations(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getConversations(user.getId()));
    }

    @GetMapping("/{partnerId}")
    public ResponseEntity<List<Message>> getConversationWithPartner(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getConversationWithPartner(user.getId(), partnerId));
    }

    @PutMapping("/{partnerId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.markAllMessagesFromPartnerAsRead(user.getId(), partnerId);
        return ResponseEntity.ok().build();
    }
}