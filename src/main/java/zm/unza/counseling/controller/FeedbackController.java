package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.UserFeedback;
import zm.unza.counseling.repository.UserFeedbackRepository;
import zm.unza.counseling.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/feedback", "/v1/feedback", "/feedback"})
@RequiredArgsConstructor
public class FeedbackController {

    private final UserFeedbackRepository feedbackRepository;
    private final UserService userService;

    /**
     * Submit feedback (any authenticated user)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserFeedback>> submitFeedback(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer rating,
            Authentication authentication) {
        
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(resolveUserId(payload, userId, authentication));
        feedback.setFeedbackType(resolveString(payload, "feedbackType", feedbackType, "GENERAL"));
        feedback.setCategory(resolveString(payload, "category", category, "general"));
        String subject = resolveString(payload, "subject", null, "");
        String message = resolveString(payload, "message", content, "");
        String finalContent = subject.isBlank() ? message : subject + "\n\n" + message;
        feedback.setContent(finalContent);
        feedback.setRating(resolveInteger(payload, "rating", rating));
        
        feedback = feedbackRepository.save(feedback);
        
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedback submitted successfully"));
    }

    /**
     * Get user's own feedback
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<UserFeedback>>> getMyFeedback(
            @RequestParam Long userId) {
        
        List<UserFeedback> feedback = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<UserFeedback>>> getCurrentUserFeedback(Authentication authentication) {
        Long resolvedUserId = resolveUserId(null, null, authentication);
        List<UserFeedback> feedback = feedbackRepository.findByUserIdOrderByCreatedAtDesc(resolvedUserId);
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    /**
     * Get all feedback (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserFeedback>>> getAllFeedback() {
        
        List<UserFeedback> feedback = feedbackRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    /**
     * Get feedback by status (admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserFeedback>>> getFeedbackByStatus(
            @PathVariable String status) {
        
        List<UserFeedback> feedback = feedbackRepository.findByStatusOrderByCreatedAtDesc(status);
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserFeedback>>> getFeedbackByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(feedbackRepository.findByCategoryOrderByCreatedAtDesc(category)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<UserFeedback>> getFeedbackById(@PathVariable Long id) {
        UserFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeedbackStats() {
        List<UserFeedback> allFeedback = feedbackRepository.findAll();
        long pending = allFeedback.stream().filter(item -> "PENDING".equalsIgnoreCase(item.getStatus())).count();
        long reviewed = allFeedback.stream().filter(item -> "REVIEWED".equalsIgnoreCase(item.getStatus())).count();
        long resolved = allFeedback.stream().filter(item -> "RESOLVED".equalsIgnoreCase(item.getStatus())).count();

        Map<String, Object> stats = Map.of(
                "total", allFeedback.size(),
                "pending", pending,
                "reviewed", reviewed,
                "resolved", resolved
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<UserFeedback>> updateFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        UserFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        if (payload.containsKey("status")) {
            feedback.setStatus(String.valueOf(payload.get("status")).toUpperCase());
        }
        if (payload.containsKey("response")) {
            feedback.setAdminResponse(String.valueOf(payload.get("response")));
        }
        if (payload.containsKey("rating")) {
            feedback.setRating(resolveInteger(payload, "rating", feedback.getRating()));
        }
        if (payload.containsKey("subject") || payload.containsKey("message")) {
            String[] parts = feedback.getContent() != null ? feedback.getContent().split("\n\n", 2) : new String[]{"", ""};
            String subject = String.valueOf(payload.getOrDefault("subject", parts.length > 0 ? parts[0] : ""));
            String message = String.valueOf(payload.getOrDefault("message", parts.length > 1 ? parts[1] : feedback.getContent()));
            feedback.setContent(subject.isBlank() ? message : subject + "\n\n" + message);
        }

        return ResponseEntity.ok(ApiResponse.success(feedbackRepository.save(feedback), "Feedback updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        feedbackRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Feedback deleted successfully"));
    }

    /**
     * Update feedback status (admin only)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserFeedback>> updateFeedbackStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String adminResponse) {
        
        UserFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        feedback.setStatus(status);
        if (adminResponse != null) {
            feedback.setAdminResponse(adminResponse);
        }
        
        feedback = feedbackRepository.save(feedback);
        
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedback status updated"));
    }

    private Long resolveUserId(Map<String, Object> payload, Long requestUserId, Authentication authentication) {
        if (requestUserId != null) {
            return requestUserId;
        }
        if (payload != null && payload.get("userId") != null) {
            return Long.valueOf(String.valueOf(payload.get("userId")));
        }
        if (authentication != null && authentication.getName() != null) {
            return userService.getUserByEmail(authentication.getName()).getId();
        }
        return 0L;
    }

    private String resolveString(Map<String, Object> payload, String key, String fallback, String defaultValue) {
        if (payload != null && payload.get(key) != null) {
            return String.valueOf(payload.get(key));
        }
        return fallback != null ? fallback : defaultValue;
    }

    private Integer resolveInteger(Map<String, Object> payload, String key, Integer fallback) {
        if (payload != null && payload.get(key) != null) {
            return Integer.valueOf(String.valueOf(payload.get(key)));
        }
        return fallback;
    }
}
