package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.KeyboardShortcut;
import zm.unza.counseling.repository.KeyboardShortcutRepository;
import zm.unza.counseling.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/keyboard-shortcuts", "/v1/keyboard-shortcuts", "/keyboard-shortcuts"})
@RequiredArgsConstructor
public class KeyboardShortcutController {

    private final KeyboardShortcutRepository shortcutRepository;
    private final UserService userService;

    /**
     * Resolve the authenticated caller's user id. Derived from the SecurityContext/Principal
     * rather than trusted from request input, to prevent a caller from acting on another
     * user's shortcuts (IDOR).
     */
    private Long currentUserId(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName()).getId();
    }

    private void assertOwner(KeyboardShortcut shortcut, Long userId) {
        if (shortcut.getUserId() == null || !shortcut.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this shortcut");
        }
    }

    /**
     * Get user's keyboard shortcuts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KeyboardShortcut>>> getShortcuts(Authentication authentication) {

        List<KeyboardShortcut> shortcuts = shortcutRepository.findByUserIdOrderByKeyAsc(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(shortcuts));
    }

    /**
     * Get enabled shortcuts only
     */
    @GetMapping("/enabled")
    public ResponseEntity<ApiResponse<List<KeyboardShortcut>>> getEnabledShortcuts(Authentication authentication) {

        List<KeyboardShortcut> shortcuts = shortcutRepository.findByUserIdAndEnabledTrue(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(shortcuts));
    }

    /**
     * Add/update a keyboard shortcut
     */
    @PostMapping
    public ResponseEntity<ApiResponse<KeyboardShortcut>> saveShortcut(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        if (payload.get("key") == null || payload.get("action") == null) {
            throw new zm.unza.counseling.exception.ValidationException("Both 'key' and 'action' are required");
        }
        String key = payload.get("key").toString();
        Long userId = currentUserId(authentication);

        // Check if shortcut already exists for this user
        KeyboardShortcut shortcut = shortcutRepository.findByUserIdAndKey(userId, key)
                .orElse(new KeyboardShortcut());

        shortcut.setUserId(userId);
        shortcut.setKey(key);
        shortcut.setAction(payload.get("action").toString());
        shortcut.setDescription(payload.getOrDefault("description", "").toString());
        shortcut.setEnabled(true);

        shortcut = shortcutRepository.save(shortcut);

        return ResponseEntity.ok(ApiResponse.success(shortcut, "Shortcut saved successfully"));
    }

    /**
     * Update shortcut (enable/disable)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KeyboardShortcut>> updateShortcut(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        KeyboardShortcut shortcut = shortcutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shortcut not found"));
        assertOwner(shortcut, currentUserId(authentication));

        if (payload.containsKey("enabled")) {
            shortcut.setEnabled(Boolean.valueOf(payload.get("enabled").toString()));
        }
        if (payload.containsKey("action")) {
            shortcut.setAction(payload.get("action").toString());
        }
        if (payload.containsKey("description")) {
            shortcut.setDescription(payload.get("description").toString());
        }

        shortcut = shortcutRepository.save(shortcut);

        return ResponseEntity.ok(ApiResponse.success(shortcut, "Shortcut updated successfully"));
    }

    /**
     * Delete a shortcut
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShortcut(@PathVariable Long id, Authentication authentication) {

        KeyboardShortcut shortcut = shortcutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shortcut not found"));
        assertOwner(shortcut, currentUserId(authentication));

        shortcutRepository.deleteById(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Shortcut deleted successfully"));
    }

    /**
     * Reset to default shortcuts
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<List<KeyboardShortcut>>> resetToDefaults(Authentication authentication) {

        Long userId = currentUserId(authentication);

        // Delete existing custom shortcuts
        List<KeyboardShortcut> existing = shortcutRepository.findByUserIdOrderByKeyAsc(userId);
        shortcutRepository.deleteAll(existing);

        // Create default shortcuts
        String[][] defaults = {
            {"Ctrl+K", "SEARCH", "Open search"},
            {"Ctrl+N", "NEW_APPOINTMENT", "Create new appointment"},
            {"Ctrl+D", "DASHBOARD", "Go to dashboard"},
            {"Ctrl+C", "CLIENTS", "Go to clients"},
            {"Ctrl+S", "SETTINGS", "Open settings"},
            {"Ctrl+H", "HOME", "Go to home"},
            {"Ctrl+E", "EXPORT", "Export data"},
            {"Ctrl+F", "FILTER", "Toggle filter"}
        };

        for (String[] d : defaults) {
            KeyboardShortcut shortcut = new KeyboardShortcut();
            shortcut.setUserId(userId);
            shortcut.setKey(d[0]);
            shortcut.setAction(d[1]);
            shortcut.setDescription(d[2]);
            shortcut.setEnabled(true);
            shortcutRepository.save(shortcut);
        }

        List<KeyboardShortcut> shortcuts = shortcutRepository.findByUserIdOrderByKeyAsc(userId);
        return ResponseEntity.ok(ApiResponse.success(shortcuts, "Shortcuts reset to defaults"));
    }
}
