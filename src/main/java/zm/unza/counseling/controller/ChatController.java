package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.ChatMessage;
import zm.unza.counseling.repository.ChatMessageRepository;
import zm.unza.counseling.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/ai-chat", "/ai-chat"})
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    /**
     * Resolve the authenticated caller's user id. Derived from the SecurityContext/Principal
     * rather than trusted from the path/body, to prevent a caller from reading, sending as, or
     * clearing another user's AI chat history (IDOR).
     */
    private Long currentUserId(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName()).getId();
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getChatHistory(@PathVariable Long userId, Authentication authentication) {
        List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(@RequestBody Map<String, String> request, Authentication authentication) {
        Long userId = currentUserId(authentication);
        String message = request.get("message");
        String sessionId = request.getOrDefault("sessionId", "default");

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserId(userId);
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessage(message);
        chatMessage.setIsFromUser(true);

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        return ResponseEntity.ok(ApiResponse.success(saved, "Message sent"));
    }

    @DeleteMapping("/history/{userId}")
    public ResponseEntity<ApiResponse> clearHistory(@PathVariable Long userId, Authentication authentication) {
        chatMessageRepository.deleteByUserId(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, "History cleared"));
    }
}
