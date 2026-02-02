package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.SettingsRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.SettingsResponse;
import zm.unza.counseling.entity.Settings;
import zm.unza.counseling.service.SettingsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/v1/settings", "/settings"})
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<SettingsResponse>> getAllSettings() {
        SettingsResponse settings = settingsService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @GetMapping("/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettingsByCategory(
            @PathVariable String category) {
        Settings.SettingCategory categoryEnum = Settings.SettingCategory.valueOf(category.toUpperCase());
        Map<String, Object> settings = settingsService.getSettingsByCategory(categoryEnum);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> updateSetting(
            @PathVariable String key,
            @RequestBody SettingsRequest request) {
        Object updatedSetting = settingsService.updateSetting(key, request);
        return ResponseEntity.ok(ApiResponse.success(updatedSetting, "Setting updated successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> createSetting(@RequestBody SettingsRequest request) {
        Object createdSetting = settingsService.createSetting(request);
        return ResponseEntity.ok(ApiResponse.success(createdSetting, "Setting created successfully"));
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<String>> deleteSetting(@PathVariable String key) {
        settingsService.deleteSetting(key);
        return ResponseEntity.ok(ApiResponse.success("Setting deleted successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Boolean>> healthCheck() {
        boolean isHealthy = settingsService.healthCheck();
        return ResponseEntity.ok(ApiResponse.success(isHealthy));
    }

    // Specific category endpoints for better API organization
    @GetMapping("/organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrganizationSettings() {
        Map<String, Object> settings = settingsService.getSettingsByCategory(Settings.SettingCategory.ORGANIZATION);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAppointmentSettings() {
        Map<String, Object> settings = settingsService.getSettingsByCategory(Settings.SettingCategory.APPOINTMENTS);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationSettings() {
        Map<String, Object> settings = settingsService.getSettingsByCategory(Settings.SettingCategory.NOTIFICATIONS);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @GetMapping("/security")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecuritySettings() {
        Map<String, Object> settings = settingsService.getSettingsByCategory(Settings.SettingCategory.SECURITY);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/organization/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> updateOrganizationSetting(
            @PathVariable String key,
            @RequestBody SettingsRequest request) {
        request.setCategory("ORGANIZATION");
        Object updatedSetting = settingsService.updateSetting(key, request);
        return ResponseEntity.ok(ApiResponse.success(updatedSetting, "Organization setting updated successfully"));
    }

    @PutMapping("/appointments/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> updateAppointmentSetting(
            @PathVariable String key,
            @RequestBody SettingsRequest request) {
        request.setCategory("APPOINTMENTS");
        Object updatedSetting = settingsService.updateSetting(key, request);
        return ResponseEntity.ok(ApiResponse.success(updatedSetting, "Appointment setting updated successfully"));
    }

    @PutMapping("/notifications/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> updateNotificationSetting(
            @PathVariable String key,
            @RequestBody SettingsRequest request) {
        request.setCategory("NOTIFICATIONS");
        Object updatedSetting = settingsService.updateSetting(key, request);
        return ResponseEntity.ok(ApiResponse.success(updatedSetting, "Notification setting updated successfully"));
    }

    @PutMapping("/security/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> updateSecuritySetting(
            @PathVariable String key,
            @RequestBody SettingsRequest request) {
        request.setCategory("SECURITY");
        Object updatedSetting = settingsService.updateSetting(key, request);
        return ResponseEntity.ok(ApiResponse.success(updatedSetting, "Security setting updated successfully"));
    }
}