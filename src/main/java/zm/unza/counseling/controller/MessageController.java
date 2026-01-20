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
@RequestMapping("/api/messages")
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
}
