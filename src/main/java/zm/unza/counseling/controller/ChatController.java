package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.ChatMessage;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.ChatMessageRepository;
import zm.unza.counseling.service.CrisisDetectionService;
import zm.unza.counseling.service.UserService;
import zm.unza.counseling.service.ai.AiAssistantService;
import zm.unza.counseling.service.ai.AiCrisisEscalationService;
import zm.unza.counseling.service.ai.AiResult;
import zm.unza.counseling.service.ai.AiUnavailableException;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping({"/api/ai-chat", "/ai-chat"})
@RequiredArgsConstructor
public class ChatController {

    private static final Set<String> STAFF_ROLES = Set.of("ROLE_COUNSELOR", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");

    static final String AI_OFFLINE_REPLY =
            "I'm not able to respond right now because the AI assistant is offline. "
            + "You can still book a session with a counselor from the Appointments page. "
            + "If you are in danger or thinking about harming yourself, please contact emergency "
            + "services or go to the nearest clinic straight away.";

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final AiAssistantService assistant;
    private final AiCrisisEscalationService crisisEscalation;

    /**
     * Resolve the authenticated caller's user id. Derived from the SecurityContext/Principal
     * rather than trusted from the path/body, to prevent a caller from reading, sending as, or
     * clearing another user's AI chat history (IDOR).
     */
    private Long currentUserId(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName()).getId();
    }

    /** Lets clients show an "AI offline" state without exposing any provider details. */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("available", assistant.isAvailable())));
    }

    /**
     * Chronological, flattened history: each stored row becomes a user turn followed by the
     * assistant turn. Shape ({@code role}/{@code sender}/{@code content}/{@code timestamp}) is
     * what both the web and Flutter chat screens read.
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChatHistory(@PathVariable Long userId, Authentication authentication) {
        List<ChatMessage> rows = new ArrayList<>(chatMessageRepository.findByUserIdOrderByCreatedAtDesc(currentUserId(authentication)));
        Collections.reverse(rows);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ChatMessage m : rows) {
            if (m.getMessage() != null && !m.getMessage().isBlank()) {
                out.add(turn(m.getId() + "-u", "user", "user", m.getMessage(), m.getCreatedAt(), m.getSessionId()));
            }
            if (m.getResponse() != null && !m.getResponse().isBlank()) {
                out.add(turn(m.getId() + "-a", "assistant", "ai", m.getResponse(), m.getCreatedAt(), m.getSessionId()));
            }
        }
        return ResponseEntity.ok(ApiResponse.success(out));
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(@RequestBody Map<String, Object> request, Authentication authentication) {
        User currentUser = userService.getUserByEmail(authentication.getName());
        Long userId = currentUser.getId();
        String message = Objects.toString(request.get("message"), "").trim();
        String sessionId = Objects.toString(request.getOrDefault("sessionId", "default"), "default");
        String context = Objects.toString(request.get("context"), "");

        if (message.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Message is required"));
        }
        if (message.length() > 4000) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Message is too long (max 4000 characters)"));
        }

        boolean sessionNotes = "session_notes".equals(context) && isStaff(authentication);
        CrisisDetectionService.CrisisResult crisisResult = sessionNotes ? null : crisisEscalation.scan(message);
        boolean crisis = crisisResult != null && crisisResult.isCrisis();

        String reply;
        boolean aiAvailable = true;
        String provider = null;
        String model = null;
        try {
            AiResult r = sessionNotes
                    ? assistant.sessionNoteFromText(message)
                    : assistant.wellnessReply(userId, sessionId, message, crisis);
            reply = r.content();
            provider = r.providerName();
            model = r.model();
        } catch (AiUnavailableException e) {
            log.warn("AI chat unavailable for user {}: {}", userId, e.getMessage());
            aiAvailable = false;
            reply = sessionNotes ? "AI note drafting is unavailable right now. Please try again later." : AI_OFFLINE_REPLY;
        }

        Long savedId = null;
        LocalDateTime createdAt = LocalDateTime.now();
        // Session-note drafts are counselor work, not the user's wellness conversation — don't mix them in.
        if (!sessionNotes) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setUserId(userId);
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessage(message);
            chatMessage.setResponse(reply);
            chatMessage.setIsFromUser(true);
            ChatMessage saved = chatMessageRepository.save(chatMessage);
            savedId = saved.getId();
            if (saved.getCreatedAt() != null) createdAt = saved.getCreatedAt();
            // Raise a CrisisAlert + notify counselors, even if the AI itself was offline.
            if (crisis) crisisEscalation.escalate(currentUser, savedId, crisisResult);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", savedId);
        body.put("sessionId", sessionId);
        body.put("userMessage", message);
        body.put("response", reply);
        body.put("content", reply);
        body.put("role", "assistant");
        body.put("sender", "ai");
        body.put("crisisDetected", crisis);
        body.put("aiAvailable", aiAvailable);
        body.put("provider", provider);
        body.put("model", model);
        body.put("timestamp", createdAt);
        return ResponseEntity.ok(ApiResponse.success(body, "Message sent"));
    }

    @DeleteMapping("/history/{userId}")
    public ResponseEntity<ApiResponse> clearHistory(@PathVariable Long userId, Authentication authentication) {
        chatMessageRepository.deleteByUserId(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, "History cleared"));
    }

    private static boolean isStaff(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(STAFF_ROLES::contains);
    }

    private static Map<String, Object> turn(String id, String role, String sender, String content,
                                            LocalDateTime ts, String sessionId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("role", role);
        m.put("sender", sender);
        m.put("content", content);
        m.put("timestamp", ts);
        m.put("sessionId", sessionId);
        return m;
    }
}
