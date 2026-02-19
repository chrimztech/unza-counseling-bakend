package zm.unza.counseling.service;

import zm.unza.counseling.dto.request.SettingsRequest;
import zm.unza.counseling.dto.response.SettingsResponse;
import zm.unza.counseling.dto.settings.AllSettingsDTO;
import zm.unza.counseling.dto.settings.AppointmentSettingsDTO;
import zm.unza.counseling.dto.settings.NotificationSettingsDTO;
import zm.unza.counseling.dto.settings.OrganizationSettingsDTO;
import zm.unza.counseling.dto.settings.SecuritySettingsDTO;
import zm.unza.counseling.entity.Settings;

import java.util.Map;

/**
 * Service interface for settings management
 */
public interface SettingsService {

    /**
     * Get all settings
     * @return all settings response
     */
    SettingsResponse getAllSettings();

    /**
     * Get all settings as structured DTOs
     * @return all settings DTO
     */
    AllSettingsDTO getAllSettingsDTO();

    /**
     * Get settings by category
     * @param category the setting category
     * @return settings map by category
     */
    Map<String, Object> getSettingsByCategory(Settings.SettingCategory category);

    /**
     * Get organization settings
     * @return organization settings DTO
     */
    OrganizationSettingsDTO getOrganizationSettings();

    /**
     * Update organization settings
     * @param dto the organization settings DTO
     * @return updated organization settings DTO
     */
    OrganizationSettingsDTO updateOrganizationSettings(OrganizationSettingsDTO dto);

    /**
     * Get appointment settings
     * @return appointment settings DTO
     */
    AppointmentSettingsDTO getAppointmentSettings();

    /**
     * Update appointment settings
     * @param dto the appointment settings DTO
     * @return updated appointment settings DTO
     */
    AppointmentSettingsDTO updateAppointmentSettings(AppointmentSettingsDTO dto);

    /**
     * Get notification settings
     * @return notification settings DTO
     */
    NotificationSettingsDTO getNotificationSettings();

    /**
     * Update notification settings
     * @param dto the notification settings DTO
     * @return updated notification settings DTO
     */
    NotificationSettingsDTO updateNotificationSettings(NotificationSettingsDTO dto);

    /**
     * Get security settings
     * @return security settings DTO
     */
    SecuritySettingsDTO getSecuritySettings();

    /**
     * Update security settings
     * @param dto the security settings DTO
     * @return updated security settings DTO
     */
    SecuritySettingsDTO updateSecuritySettings(SecuritySettingsDTO dto);

    /**
     * Update setting
     * @param key the setting key
     * @param request the settings request
     * @return the updated setting
     */
    Settings updateSetting(String key, SettingsRequest request);

    /**
     * Create setting
     * @param request the settings request
     * @return the created setting
     */
    Settings createSetting(SettingsRequest request);

    /**
     * Delete setting
     * @param key the setting key
     */
    void deleteSetting(String key);

    /**
     * Get setting by key
     * @param key the setting key
     * @return the setting
     */
    Settings getSetting(String key);

    /**
     * Health check
     * @return true if healthy, false otherwise
     */
    boolean healthCheck();
}