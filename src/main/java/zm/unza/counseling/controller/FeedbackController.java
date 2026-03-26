package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.UserFeedback;
import zm.unza.counseling.repository.UserFeedbackRepository;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/feedback", "/v1/feedback", "/feedback"})
@RequiredArgsConstructor
public class FeedbackController {

    private final UserFeedbackRepository feedbackRepository;

    /**
     * Submit feedback (any authenticated user)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserFeedback>> submitFeedback(
            @RequestParam Long userId,
            @RequestParam String feedbackType,
            @RequestParam String category,
            @RequestParam String content,
            @RequestParam(required = false) Integer rating) {
        
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setFeedbackType(feedbackType);
        feedback.setCategory(category);
        feedback.setContent(content);
        feedback.setRating(rating);
        
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
}
