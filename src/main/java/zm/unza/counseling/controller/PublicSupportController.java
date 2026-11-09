package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.AiAssistantSettings;
import zm.unza.counseling.repository.AiAssistantSettingsRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Unauthenticated crisis contacts for the login screen, so someone in distress sees where to
 * get help before (or without) signing in. Returns only the crisis-contacts text a super admin
 * enters under AI Knowledge Base → Contacts & instructions — information meant to be public.
 */
@RestController
@RequestMapping({"/api/public/support-contacts", "/public/support-contacts"})
@RequiredArgsConstructor
public class PublicSupportController {

    private final AiAssistantSettingsRepository settingsRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> supportContacts() {
        String contacts = settingsRepository.findById(AiAssistantSettings.SINGLETON_ID)
                .map(AiAssistantSettings::getCrisisContacts)
                .filter(s -> !s.isBlank())
                .orElse(null);
        Map<String, Object> body = new HashMap<>();
        body.put("crisisContacts", contacts);
        return ResponseEntity.ok(ApiResponse.success(body));
    }
}
