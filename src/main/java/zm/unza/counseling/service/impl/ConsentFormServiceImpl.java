package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.ConsentFormRequest;
import zm.unza.counseling.dto.request.SignConsentRequest;
import zm.unza.counseling.dto.response.ConsentFormResponse;
import zm.unza.counseling.dto.response.UserConsentResponse;
import zm.unza.counseling.entity.ConsentForm;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.entity.UserConsent;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.ConsentFormRepository;
import zm.unza.counseling.repository.UserConsentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.ConsentFormService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ConsentFormService
 */
@Service
@RequiredArgsConstructor
public class ConsentFormServiceImpl implements ConsentFormService {

    private final ConsentFormRepository consentFormRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ConsentFormResponse createConsentForm(ConsentFormRequest request) {
        // Check if version already exists
        if (consentFormRepository.findByVersion(request.getVersion()).isPresent()) {
            throw new ValidationException("Consent form with version " + request.getVersion() + " already exists");
        }

        ConsentForm consentForm = ConsentForm.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .version(request.getVersion())
                .active(request.getActive())
                .effectiveDate(request.getEffectiveDate())
                .build();

        consentForm = consentFormRepository.save(consentForm);
        return mapToResponse(consentForm);
    }

    @Override
    @Transactional
    public ConsentFormResponse updateConsentForm(Long id, ConsentFormRequest request) {
        ConsentForm consentForm = consentFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent form not found with id: " + id));

        // Check if version already exists for other consent forms
        consentFormRepository.findByVersion(request.getVersion())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ValidationException("Consent form with version " + request.getVersion() + " already exists");
                    }
                });

        consentForm.setTitle(request.getTitle());
        consentForm.setContent(request.getContent());
        consentForm.setVersion(request.getVersion());
        consentForm.setActive(request.getActive());
        consentForm.setEffectiveDate(request.getEffectiveDate());

        consentForm = consentFormRepository.save(consentForm);
        return mapToResponse(consentForm);
    }

    @Override
    public ConsentFormResponse getConsentForm(Long id) {
        ConsentForm consentForm = consentFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent form not found with id: " + id));
        return mapToResponse(consentForm);
    }

    @Override
    public List<ConsentFormResponse> getAllConsentForms() {
        return consentFormRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsentFormResponse> getActiveConsentForms() {
        return consentFormRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ConsentFormResponse> getLatestActiveConsentForm() {
        List<ConsentForm> forms = consentFormRepository.findLatestActive();
        if (forms.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToResponse(forms.get(0)));
    }

    @Override
    @Transactional
    public UserConsentResponse signConsentForm(Long userId, SignConsentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ConsentForm consentForm = consentFormRepository.findById(request.getConsentFormId())
                .orElseThrow(() -> new ResourceNotFoundException("Consent form not found with id: " + request.getConsentFormId()));

        // Check if user has already signed this consent form
        if (userConsentRepository.existsByUserAndConsentForm(user, consentForm)) {
            throw new ValidationException("User has already signed this consent form");
        }

        UserConsent userConsent = UserConsent.builder()
                .user(user)
                .consentForm(consentForm)
                .consentDate(LocalDateTime.now())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .build();

        userConsent = userConsentRepository.save(userConsent);

        // Update user's consent status
        user.setHasSignedConsent(true);
        userRepository.save(user);

        return mapToUserConsentResponse(userConsent);
    }

    @Override
    public boolean hasUserSignedLatestConsent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<ConsentForm> activeForms = consentFormRepository.findLatestActive();
        if (activeForms.isEmpty()) {
            return true; // No consent forms required
        }

        ConsentForm latestForm = activeForms.get(0);
        return userConsentRepository.existsByUserAndConsentForm(user, latestForm);
    }

    @Override
    public List<UserConsentResponse> getUserConsentHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userConsentRepository.findByUserOrderByConsentDateDesc(user).stream()
                .map(this::mapToUserConsentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConsentStatistics getConsentStatistics() {
        long totalForms = consentFormRepository.count();
        long activeForms = consentFormRepository.findByActiveTrue().size();
        long totalSignatures = userConsentRepository.count();
        long uniqueSigners = userConsentRepository.countDistinctUsers();

        return new ConsentStatistics(totalForms, activeForms, totalSignatures, uniqueSigners);
    }

    @Override
    @Transactional
    public void deleteConsentForm(Long id) {
        ConsentForm consentForm = consentFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent form not found with id: " + id));

        // Remove all user consents for this form
        userConsentRepository.deleteByConsentForm(consentForm);

        consentFormRepository.delete(consentForm);
    }

    @Override
    @Transactional
    public void activateConsentForm(Long id) {
        ConsentForm consentForm = consentFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent form not found with id: " + id));

        // Deactivate all other consent forms of the same type if needed
        // For now, just activate this one
        consentForm.setActive(true);
        consentFormRepository.save(consentForm);
    }

    @Override
    @Transactional
    public void deactivateConsentForm(Long id) {
        ConsentForm consentForm = consentFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent form not found with id: " + id));

        consentForm.setActive(false);
        consentFormRepository.save(consentForm);
    }

    // Helper methods
    private ConsentFormResponse mapToResponse(ConsentForm consentForm) {
        return ConsentFormResponse.builder()
                .id(consentForm.getId())
                .title(consentForm.getTitle())
                .content(consentForm.getContent())
                .version(consentForm.getVersion())
                .active(consentForm.getActive())
                .effectiveDate(consentForm.getEffectiveDate())
                .createdAt(consentForm.getCreatedAt())
                .updatedAt(consentForm.getUpdatedAt())
                .build();
    }

    private UserConsentResponse mapToUserConsentResponse(UserConsent userConsent) {
        return UserConsentResponse.builder()
                .id(userConsent.getId())
                .userId(userConsent.getUser().getId())
                .consentFormId(userConsent.getConsentForm().getId())
                .consentFormTitle(userConsent.getConsentForm().getTitle())
                .consentFormVersion(userConsent.getConsentForm().getVersion())
                .consentDate(userConsent.getConsentDate())
                .ipAddress(userConsent.getIpAddress())
                .userAgent(userConsent.getUserAgent())
                .createdAt(userConsent.getCreatedAt())
                .build();
    }
}