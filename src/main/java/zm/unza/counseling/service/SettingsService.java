package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.SettingsRequest;
import zm.unza.counseling.dto.response.SettingsResponse;
import zm.unza.counseling.entity.Settings;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.SettingsRepository;

import java.util.*;
import java.util.stream.Collectors;

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
     * Get settings by category
     * @param category the setting category
     * @return settings map by category
     */
    Map<String, Object> getSettingsByCategory(Settings.SettingCategory category);

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