package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.SettingsRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.SettingsResponse;
import zm.unza.counseling.dto.settings.AllSettingsDTO;
import zm.unza.counseling.dto.settings.AppointmentSettingsDTO;
import zm.unza.counseling.dto.settings.NotificationSettingsDTO;
import zm.unza.counseling.dto.settings.OrganizationSettingsDTO;
import zm.unza.counseling.dto.settings.SecuritySettingsDTO;
import zm.unza.counseling.entity.Settings;
import zm.unza.counseling.service.SettingsService;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/settings", "/v1/settings", "/settings"})
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * GET /api/v1/settings - Get all settings
     * Returns all settings grouped by category
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<AllSettingsDTO>> getAllSettings() {
        AllSettingsDTO settings = settingsService.getAllSettingsDTO();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * GET /api/v1/settings/organization - Get organization settings
     */
    @GetMapping("/organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<OrganizationSettingsDTO>> getOrganizationSettings() {
        OrganizationSettingsDTO settings = settingsService.getOrganizationSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * PUT /api/v1/settings/organization - Update organization settings
     */
    @PutMapping("/organization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationSettingsDTO>> updateOrganizationSettings(
            @Valid @RequestBody OrganizationSettingsDTO request) {
        OrganizationSettingsDTO updated = settingsService.updateOrganizationSettings(request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Organization settings updated successfully"));
    }

    /**
     * GET /api/v1/settings/appointments - Get appointment settings
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<AppointmentSettingsDTO>> getAppointmentSettings() {
        AppointmentSettingsDTO settings = settingsService.getAppointmentSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * PUT /api/v1/settings/appointments - Update appointment settings
     */
    @PutMapping("/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentSettingsDTO>> updateAppointmentSettings(
            @Valid @RequestBody AppointmentSettingsDTO request) {
        AppointmentSettingsDTO updated = settingsService.updateAppointmentSettings(request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Appointment settings updated successfully"));
    }

    /**
     * GET /api/v1/settings/notifications - Get notification settings
     */
    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<NotificationSettingsDTO>> getNotificationSettings() {
        NotificationSettingsDTO settings = settingsService.getNotificationSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * PUT /api/v1/settings/notifications - Update notification settings
     */
    @PutMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationSettingsDTO>> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingsDTO request) {
        NotificationSettingsDTO updated = settingsService.updateNotificationSettings(request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification settings updated successfully"));
    }

    /**
     * GET /api/v1/settings/security - Get security settings
     */
    @GetMapping("/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SecuritySettingsDTO>> getSecuritySettings() {
        SecuritySettingsDTO settings = settingsService.getSecuritySettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * PUT /api/v1/settings/security - Update security settings
     */
    @PutMapping("/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SecuritySettingsDTO>> updateSecuritySettings(
            @Valid @RequestBody SecuritySettingsDTO request) {
        SecuritySettingsDTO updated = settingsService.updateSecuritySettings(request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Security settings updated successfully"));
    }

    // Legacy endpoints for backward compatibility
    
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

    /**
     * PUT /api/v1/settings - Update all settings at once
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AllSettingsDTO>> updateAllSettings(
            @Valid @RequestBody AllSettingsDTO request) {
        AllSettingsDTO updated = settingsService.updateAllSettings(request);
        return ResponseEntity.ok(ApiResponse.success(updated, "All settings updated successfully"));
    }

    /**
     * PUT /api/v1/settings/{category}/{key} - Update setting by category and key
     */
    @PutMapping("/{category}/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> updateSettingByKey(
            @PathVariable String category,
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        Object value = request.get("value");
        Settings.SettingCategory categoryEnum = Settings.SettingCategory.valueOf(category.toUpperCase());
        Object result = settingsService.updateSettingByCategoryAndKey(categoryEnum, key, value);
        return ResponseEntity.ok(ApiResponse.success(result, "Setting updated successfully"));
    }

    /**
     * PUT /api/v1/settings/security/{key} - Update security setting by key
     */
    @PutMapping("/security/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SecuritySettingsDTO>> updateSecuritySetting(
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        Object value = request.get(key);
        if (value == null) {
            value = request.get("value");
        }
        SecuritySettingsDTO updated = settingsService.updateSecuritySettingByKey(key, value);
        return ResponseEntity.ok(ApiResponse.success(updated, "Security setting updated successfully"));
    }

    /**
     * PUT /api/v1/settings/organization/{key} - Update organization setting by key
     */
    @PutMapping("/organization/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationSettingsDTO>> updateOrganizationSetting(
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        Object value = request.get(key);
        if (value == null) {
            value = request.get("value");
        }
        OrganizationSettingsDTO updated = settingsService.updateOrganizationSettingByKey(key, value);
        return ResponseEntity.ok(ApiResponse.success(updated, "Organization setting updated successfully"));
    }

    /**
     * PUT /api/v1/settings/notifications/{key} - Update notification setting by key
     */
    @PutMapping("/notifications/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationSettingsDTO>> updateNotificationSetting(
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        Object value = request.get(key);
        if (value == null) {
            value = request.get("value");
        }
        NotificationSettingsDTO updated = settingsService.updateNotificationSettingByKey(key, value);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification setting updated successfully"));
    }

    /**
     * PUT /api/v1/settings/appointments/{key} - Update appointment setting by key
     */
    @PutMapping("/appointments/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentSettingsDTO>> updateAppointmentSetting(
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        Object value = request.get(key);
        if (value == null) {
            value = request.get("value");
        }
        AppointmentSettingsDTO updated = settingsService.updateAppointmentSettingByKey(key, value);
        return ResponseEntity.ok(ApiResponse.success(updated, "Appointment setting updated successfully"));
    }
}
