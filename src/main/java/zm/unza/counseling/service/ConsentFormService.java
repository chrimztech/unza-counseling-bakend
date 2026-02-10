package zm.unza.counseling.service;

import zm.unza.counseling.dto.request.ConsentFormRequest;
import zm.unza.counseling.dto.request.SignConsentRequest;
import zm.unza.counseling.dto.response.ConsentFormResponse;
import zm.unza.counseling.dto.response.UserConsentResponse;
import zm.unza.counseling.entity.ConsentForm;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.entity.UserConsent;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for consent form management
 */
public interface ConsentFormService {

    /**
     * Create a new consent form
     */
    ConsentFormResponse createConsentForm(ConsentFormRequest request);

    /**
     * Update an existing consent form
     */
    ConsentFormResponse updateConsentForm(Long id, ConsentFormRequest request);

    /**
     * Get consent form by ID
     */
    ConsentFormResponse getConsentForm(Long id);

    /**
     * Get all consent forms
     */
    List<ConsentFormResponse> getAllConsentForms();

    /**
     * Get active consent forms
     */
    List<ConsentFormResponse> getActiveConsentForms();

    /**
     * Get the latest active consent form
     */
    Optional<ConsentFormResponse> getLatestActiveConsentForm();

    /**
     * Sign a consent form
     */
    UserConsentResponse signConsentForm(Long userId, SignConsentRequest request);

    /**
     * Check if user has signed the latest active consent form
     */
    boolean hasUserSignedLatestConsent(Long userId);

    /**
     * Get user's consent history
     */
    List<UserConsentResponse> getUserConsentHistory(Long userId);

    /**
     * Get consent statistics
     */
    ConsentStatistics getConsentStatistics();

    /**
     * Delete a consent form
     */
    void deleteConsentForm(Long id);

    /**
     * Activate a consent form
     */
    void activateConsentForm(Long id);

    /**
     * Deactivate a consent form
     */
    void deactivateConsentForm(Long id);

    /**
     * Statistics for consent forms
     */
    class ConsentStatistics {
        private long totalForms;
        private long activeForms;
        private long totalSignatures;
        private long uniqueSigners;

        public ConsentStatistics(long totalForms, long activeForms, long totalSignatures, long uniqueSigners) {
            this.totalForms = totalForms;
            this.activeForms = activeForms;
            this.totalSignatures = totalSignatures;
            this.uniqueSigners = uniqueSigners;
        }

        // Getters
        public long getTotalForms() { return totalForms; }
        public long getActiveForms() { return activeForms; }
        public long getTotalSignatures() { return totalSignatures; }
        public long getUniqueSigners() { return uniqueSigners; }
    }
}