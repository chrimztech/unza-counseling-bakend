package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.SettingsRequest;
import zm.unza.counseling.dto.response.SettingsResponse;
import zm.unza.counseling.entity.Settings;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.SettingsRepository;
import zm.unza.counseling.service.SettingsService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SettingsService
 */
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SettingsRepository settingsRepository;

    @Override
    @Transactional(readOnly = true)
    public SettingsResponse getAllSettings() {
        List<Settings> allSettings = settingsRepository.findAll();
        
        // Group settings by category
        Map<String, List<SettingsResponse.SettingResponse>> settingsByCategory = allSettings.stream()
                .collect(Collectors.groupingBy(
                        settings -> settings.getCategory().name().toLowerCase(),
                        Collectors.mapping(this::convertToSettingResponse, Collectors.toList())
                ));

        // Convert to the response structure
        List<SettingsResponse.SettingsCategoryResponse> allSettingsList = settingsByCategory.entrySet().stream()
                .map(entry -> {
                    SettingsResponse.SettingsCategoryResponse response = new SettingsResponse.SettingsCategoryResponse();
                    response.setCategory(entry.getKey());
                    response.setSettings(entry.getValue());
                    return response;
                })
                .collect(Collectors.toList());

        SettingsResponse response = new SettingsResponse();
        response.setOrganization(settingsByCategory.getOrDefault("organization", Collections.emptyList()).stream()
                .collect(Collectors.toMap(SettingsResponse.SettingResponse::getKey, sr -> sr.getValue())));
        response.setAppointments(settingsByCategory.getOrDefault("appointments", Collections.emptyList()).stream()
                .collect(Collectors.toMap(SettingsResponse.SettingResponse::getKey, sr -> sr.getValue())));
        response.setNotifications(settingsByCategory.getOrDefault("notifications", Collections.emptyList()).stream()
                .collect(Collectors.toMap(SettingsResponse.SettingResponse::getKey, sr -> sr.getValue())));
        response.setSecurity(settingsByCategory.getOrDefault("security", Collections.emptyList()).stream()
                .collect(Collectors.toMap(SettingsResponse.SettingResponse::getKey, sr -> sr.getValue())));
        response.setAllSettings(allSettingsList);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSettingsByCategory(Settings.SettingCategory category) {
        List<Settings> settings = settingsRepository.findByCategory(category);
        
        return settings.stream()
                .collect(Collectors.toMap(
                        Settings::getKey,
                        setting -> convertValue(setting)
                ));
    }

    @Override
    @Transactional
    public Settings updateSetting(String key, SettingsRequest request) {
        Settings existingSetting = settingsRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        existingSetting.setValue(request.getValue().toString());
        existingSetting.setDescription(request.getDescription());
        existingSetting.setCategory(Settings.SettingCategory.valueOf(request.getCategory().toUpperCase()));
        existingSetting.setType(Settings.SettingType.valueOf(request.getType().toUpperCase()));
        existingSetting.setActive(request.isActive());

        return settingsRepository.save(existingSetting);
    }

    @Override
    @Transactional
    public Settings createSetting(SettingsRequest request) {
        // Check if setting with key already exists
        Optional<Settings> existingSetting = settingsRepository.findByKey(request.getKey());
        if (existingSetting.isPresent()) {
            throw new IllegalArgumentException("Setting with key '" + request.getKey() + "' already exists");
        }

        Settings newSetting = new Settings();
        newSetting.setKey(request.getKey());
        newSetting.setValue(request.getValue().toString());
        newSetting.setDescription(request.getDescription());
        newSetting.setCategory(Settings.SettingCategory.valueOf(request.getCategory().toUpperCase()));
        newSetting.setType(Settings.SettingType.valueOf(request.getType().toUpperCase()));
        newSetting.setActive(request.isActive());

        return settingsRepository.save(newSetting);
    }

    @Override
    @Transactional
    public void deleteSetting(String key) {
        Settings setting = settingsRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));
        
        settingsRepository.delete(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public Settings getSetting(String key) {
        return settingsRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean healthCheck() {
        try {
            // Simple health check - try to fetch count of settings
            long count = settingsRepository.count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SettingsResponse.SettingResponse convertToSettingResponse(Settings settings) {
        SettingsResponse.SettingResponse response = new SettingsResponse.SettingResponse();
        response.setKey(settings.getKey());
        response.setValue(convertValue(settings));
        response.setType(settings.getType().name().toLowerCase());
        response.setDescription(settings.getDescription());
        response.setActive(settings.isActive());
        return response;
    }

    private Object convertValue(Settings settings) {
        if (settings.getValue() == null) {
            return null;
        }
        
        switch (settings.getType()) {
            case BOOLEAN:
                return Boolean.parseBoolean(settings.getValue());
            case INTEGER:
                return Integer.parseInt(settings.getValue());
            default:
                return settings.getValue();
        }
    }
}