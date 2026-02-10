package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.ConsentFormRequest;
import zm.unza.counseling.dto.request.SignConsentRequest;
import zm.unza.counseling.dto.response.ConsentFormResponse;
import zm.unza.counseling.dto.response.UserConsentResponse;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.ConsentFormService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for consent form management
 */
@RestController
@RequestMapping({"/v1/consent", "/consent"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConsentFormController {

    private final ConsentFormService consentFormService;
    private final UserRepository userRepository;

    /**
     * Create a new consent form
     */
    @PostMapping("/forms")
    public ResponseEntity<ConsentFormResponse> createConsentForm(@RequestBody ConsentFormRequest request) {
        ConsentFormResponse response = consentFormService.createConsentForm(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing consent form
     */
    @PutMapping("/forms/{id}")
    public ResponseEntity<ConsentFormResponse> updateConsentForm(@PathVariable Long id, @RequestBody ConsentFormRequest request) {
        ConsentFormResponse response = consentFormService.updateConsentForm(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get consent form by ID
     */
    @GetMapping("/forms/{id}")
    public ResponseEntity<ConsentFormResponse> getConsentForm(@PathVariable Long id) {
        ConsentFormResponse response = consentFormService.getConsentForm(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all consent forms
     */
    @GetMapping("/forms")
    public ResponseEntity<List<ConsentFormResponse>> getAllConsentForms() {
        List<ConsentFormResponse> responses = consentFormService.getAllConsentForms();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get active consent forms
     */
    @GetMapping("/forms/active")
    public ResponseEntity<List<ConsentFormResponse>> getActiveConsentForms() {
        List<ConsentFormResponse> responses = consentFormService.getActiveConsentForms();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get the latest active consent form
     */
    @GetMapping("/forms/latest")
    public ResponseEntity<ConsentFormResponse> getLatestActiveConsentForm() {
        Optional<ConsentFormResponse> response = consentFormService.getLatestActiveConsentForm();
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Sign a consent form
     */
    @PostMapping("/sign")
    public ResponseEntity<UserConsentResponse> signConsentForm(@RequestBody SignConsentRequest request, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = user.getId();

        UserConsentResponse response = consentFormService.signConsentForm(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has signed the latest active consent form
     */
    @GetMapping("/check-signed")
    public ResponseEntity<Boolean> hasUserSignedLatestConsent(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = user.getId();

        boolean hasSigned = consentFormService.hasUserSignedLatestConsent(userId);
        return ResponseEntity.ok(hasSigned);
    }

    /**
     * Get user's consent history
     */
    @GetMapping("/history")
    public ResponseEntity<List<UserConsentResponse>> getUserConsentHistory(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = user.getId();

        List<UserConsentResponse> responses = consentFormService.getUserConsentHistory(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get consent statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ConsentFormService.ConsentStatistics> getConsentStatistics() {
        ConsentFormService.ConsentStatistics statistics = consentFormService.getConsentStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Delete a consent form
     */
    @DeleteMapping("/forms/{id}")
    public ResponseEntity<Void> deleteConsentForm(@PathVariable Long id) {
        consentFormService.deleteConsentForm(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate a consent form
     */
    @PostMapping("/forms/{id}/activate")
    public ResponseEntity<Void> activateConsentForm(@PathVariable Long id) {
        consentFormService.activateConsentForm(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Deactivate a consent form
     */
    @PostMapping("/forms/{id}/deactivate")
    public ResponseEntity<Void> deactivateConsentForm(@PathVariable Long id) {
        consentFormService.deactivateConsentForm(id);
        return ResponseEntity.ok().build();
    }
}