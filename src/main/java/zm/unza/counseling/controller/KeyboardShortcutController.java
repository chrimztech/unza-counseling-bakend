package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.KeyboardShortcut;
import zm.unza.counseling.repository.KeyboardShortcutRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/keyboard-shortcuts", "/v1/keyboard-shortcuts", "/keyboard-shortcuts"})
@RequiredArgsConstructor
public class KeyboardShortcutController {

    private final KeyboardShortcutRepository shortcutRepository;

    /**
     * Get user's keyboard shortcuts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KeyboardShortcut>>> getShortcuts(
            @RequestParam Long userId) {
        
        List<KeyboardShortcut> shortcuts = shortcutRepository.findByUserIdOrderByKeyAsc(userId);
        return ResponseEntity.ok(ApiResponse.success(shortcuts));
    }

    /**
     * Get enabled shortcuts only
     */
    @GetMapping("/enabled")
    public ResponseEntity<ApiResponse<List<KeyboardShortcut>>> getEnabledShortcuts(
            @RequestParam Long userId) {
        
        List<KeyboardShortcut> shortcuts = shortcutRepository.findByUserIdAndEnabledTrue(userId);
        return ResponseEntity.ok(ApiResponse.success(shortcuts));
    }

    /**
     * Add/update a keyboard shortcut
     */
    @PostMapping
    public ResponseEntity<ApiResponse<KeyboardShortcut>> saveShortcut(
            @RequestBody Map<String, Object> payload) {
        
        String key = payload.get("key").toString();
        Long userId = Long.valueOf(payload.get("userId").toString());
        
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
            @RequestBody Map<String, Object> payload) {
        
        KeyboardShortcut shortcut = shortcutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shortcut not found"));
        
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
    public ResponseEntity<ApiResponse<Void>> deleteShortcut(@PathVariable Long id) {
        
        shortcutRepository.deleteById(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Shortcut deleted successfully"));
    }

    /**
     * Reset to default shortcuts
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<List<KeyboardShortcut>>> resetToDefaults(
            @RequestParam Long userId) {
        
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
