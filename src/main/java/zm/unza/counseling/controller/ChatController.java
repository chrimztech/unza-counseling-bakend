package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.ChatMessage;
import zm.unza.counseling.repository.ChatMessageRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/ai-chat", "/ai-chat"})
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getChatHistory(@PathVariable Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
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
    public ResponseEntity<ApiResponse> clearHistory(@PathVariable Long userId) {
        chatMessageRepository.deleteByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "History cleared"));
    }
}