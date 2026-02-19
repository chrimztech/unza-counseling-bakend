package zm.unza.counseling.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.SettingsRequest;
import zm.unza.counseling.dto.response.SettingsResponse;
import zm.unza.counseling.dto.settings.AllSettingsDTO;
import zm.unza.counseling.dto.settings.AppointmentSettingsDTO;
import zm.unza.counseling.dto.settings.NotificationSettingsDTO;
import zm.unza.counseling.dto.settings.OrganizationSettingsDTO;
import zm.unza.counseling.dto.settings.SecuritySettingsDTO;
import zm.unza.counseling.entity.Settings;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.SettingsRepository;
import zm.unza.counseling.service.SettingsService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SettingsService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    // Setting keys constants
    private static final String ORG_NAME = "organizationName";
    private static final String ORG_EMAIL = "contactEmail";
    private static final String ORG_PHONE = "contactPhone";
    private static final String ORG_HOURS = "operatingHours";
    
    private static final String APPT_DURATION = "defaultSessionDuration";
    private static final String APPT_MAX_DAYS = "maxAdvanceBookingDays";
    private static final String APPT_CANCEL_HOURS = "cancellationDeadlineHours";
    private static final String APPT_AUTO_CONFIRM = "autoConfirmAppointments";
    
    private static final String NOTIF_APPT_REMINDERS = "appointmentReminders";
    private static final String NOTIF_FOLLOW_UP = "followUpNotifications";
    private static final String NOTIF_WEEKLY = "weeklySummary";
    private static final String NOTIF_EMAIL = "emailNotifications";
    private static final String NOTIF_SMS = "smsNotifications";
    
    private static final String SEC_RETENTION = "dataRetentionPeriod";
    private static final String SEC_ENCRYPTION = "encryptionEnabled";
    private static final String SEC_AUDIT = "auditLoggingEnabled";

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
    public AllSettingsDTO getAllSettingsDTO() {
        AllSettingsDTO dto = new AllSettingsDTO();
        dto.setOrganization(getOrganizationSettings());
        dto.setAppointments(getAppointmentSettings());
        dto.setNotifications(getNotificationSettings());
        dto.setSecurity(getSecuritySettings());
        return dto;
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
    @Transactional(readOnly = true)
    public OrganizationSettingsDTO getOrganizationSettings() {
        OrganizationSettingsDTO dto = new OrganizationSettingsDTO();
        dto.setOrganizationName(getStringValue(ORG_NAME, Settings.SettingCategory.ORGANIZATION, "University of Zambia"));
        dto.setContactEmail(getStringValue(ORG_EMAIL, Settings.SettingCategory.ORGANIZATION, "counseling@unza.zm"));
        dto.setContactPhone(getStringValue(ORG_PHONE, Settings.SettingCategory.ORGANIZATION, "+260-977-123456"));
        
        // Get operating hours
        OrganizationSettingsDTO.OperatingHoursDTO hours = new OrganizationSettingsDTO.OperatingHoursDTO();
        String hoursJson = getStringValue(ORG_HOURS, Settings.SettingCategory.ORGANIZATION, null);
        if (hoursJson != null) {
            try {
                hours = objectMapper.readValue(hoursJson, OrganizationSettingsDTO.OperatingHoursDTO.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse operating hours, using defaults", e);
                hours.setOpenTime("08:00");
                hours.setCloseTime("17:00");
            }
        } else {
            hours.setOpenTime("08:00");
            hours.setCloseTime("17:00");
        }
        dto.setOperatingHours(hours);
        
        return dto;
    }

    @Override
    @Transactional
    public OrganizationSettingsDTO updateOrganizationSettings(OrganizationSettingsDTO dto) {
        saveSetting(ORG_NAME, dto.getOrganizationName(), Settings.SettingType.STRING, Settings.SettingCategory.ORGANIZATION);
        saveSetting(ORG_EMAIL, dto.getContactEmail(), Settings.SettingType.STRING, Settings.SettingCategory.ORGANIZATION);
        saveSetting(ORG_PHONE, dto.getContactPhone(), Settings.SettingType.STRING, Settings.SettingCategory.ORGANIZATION);
        
        if (dto.getOperatingHours() != null) {
            try {
                String hoursJson = objectMapper.writeValueAsString(dto.getOperatingHours());
                saveSetting(ORG_HOURS, hoursJson, Settings.SettingType.JSON, Settings.SettingCategory.ORGANIZATION);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize operating hours", e);
            }
        }
        
        return getOrganizationSettings();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentSettingsDTO getAppointmentSettings() {
        AppointmentSettingsDTO dto = new AppointmentSettingsDTO();
        dto.setDefaultSessionDuration(getIntValue(APPT_DURATION, Settings.SettingCategory.APPOINTMENTS, 50));
        dto.setMaxAdvanceBookingDays(getIntValue(APPT_MAX_DAYS, Settings.SettingCategory.APPOINTMENTS, 30));
        dto.setCancellationDeadlineHours(getIntValue(APPT_CANCEL_HOURS, Settings.SettingCategory.APPOINTMENTS, 24));
        dto.setAutoConfirmAppointments(getBoolValue(APPT_AUTO_CONFIRM, Settings.SettingCategory.APPOINTMENTS, true));
        return dto;
    }

    @Override
    @Transactional
    public AppointmentSettingsDTO updateAppointmentSettings(AppointmentSettingsDTO dto) {
        saveSetting(APPT_DURATION, String.valueOf(dto.getDefaultSessionDuration()), Settings.SettingType.INTEGER, Settings.SettingCategory.APPOINTMENTS);
        saveSetting(APPT_MAX_DAYS, String.valueOf(dto.getMaxAdvanceBookingDays()), Settings.SettingType.INTEGER, Settings.SettingCategory.APPOINTMENTS);
        saveSetting(APPT_CANCEL_HOURS, String.valueOf(dto.getCancellationDeadlineHours()), Settings.SettingType.INTEGER, Settings.SettingCategory.APPOINTMENTS);
        saveSetting(APPT_AUTO_CONFIRM, String.valueOf(dto.getAutoConfirmAppointments()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPOINTMENTS);
        return getAppointmentSettings();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSettingsDTO getNotificationSettings() {
        NotificationSettingsDTO dto = new NotificationSettingsDTO();
        dto.setAppointmentReminders(getBoolValue(NOTIF_APPT_REMINDERS, Settings.SettingCategory.NOTIFICATIONS, true));
        dto.setFollowUpNotifications(getBoolValue(NOTIF_FOLLOW_UP, Settings.SettingCategory.NOTIFICATIONS, true));
        dto.setWeeklySummary(getBoolValue(NOTIF_WEEKLY, Settings.SettingCategory.NOTIFICATIONS, false));
        dto.setEmailNotifications(getBoolValue(NOTIF_EMAIL, Settings.SettingCategory.NOTIFICATIONS, true));
        dto.setSmsNotifications(getBoolValue(NOTIF_SMS, Settings.SettingCategory.NOTIFICATIONS, false));
        return dto;
    }

    @Override
    @Transactional
    public NotificationSettingsDTO updateNotificationSettings(NotificationSettingsDTO dto) {
        saveSetting(NOTIF_APPT_REMINDERS, String.valueOf(dto.getAppointmentReminders()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
        saveSetting(NOTIF_FOLLOW_UP, String.valueOf(dto.getFollowUpNotifications()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
        saveSetting(NOTIF_WEEKLY, String.valueOf(dto.getWeeklySummary()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
        saveSetting(NOTIF_EMAIL, String.valueOf(dto.getEmailNotifications()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
        saveSetting(NOTIF_SMS, String.valueOf(dto.getSmsNotifications()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
        return getNotificationSettings();
    }

    @Override
    @Transactional(readOnly = true)
    public SecuritySettingsDTO getSecuritySettings() {
        SecuritySettingsDTO dto = new SecuritySettingsDTO();
        dto.setDataRetentionPeriod(getIntValue(SEC_RETENTION, Settings.SettingCategory.SECURITY, 5));
        dto.setEncryptionEnabled(getBoolValue(SEC_ENCRYPTION, Settings.SettingCategory.SECURITY, true));
        dto.setAuditLoggingEnabled(getBoolValue(SEC_AUDIT, Settings.SettingCategory.SECURITY, true));
        return dto;
    }

    @Override
    @Transactional
    public SecuritySettingsDTO updateSecuritySettings(SecuritySettingsDTO dto) {
        saveSetting(SEC_RETENTION, String.valueOf(dto.getDataRetentionPeriod()), Settings.SettingType.INTEGER, Settings.SettingCategory.SECURITY);
        saveSetting(SEC_ENCRYPTION, String.valueOf(dto.getEncryptionEnabled()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.SECURITY);
        saveSetting(SEC_AUDIT, String.valueOf(dto.getAuditLoggingEnabled()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.SECURITY);
        return getSecuritySettings();
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

    // Helper methods
    private void saveSetting(String key, String value, Settings.SettingType type, Settings.SettingCategory category) {
        Optional<Settings> existingSetting = settingsRepository.findByKey(key);
        
        if (existingSetting.isPresent()) {
            Settings setting = existingSetting.get();
            setting.setValue(value);
            setting.setType(type);
            settingsRepository.save(setting);
        } else {
            Settings newSetting = new Settings();
            newSetting.setKey(key);
            newSetting.setValue(value);
            newSetting.setType(type);
            newSetting.setCategory(category);
            newSetting.setActive(true);
            settingsRepository.save(newSetting);
        }
    }

    private String getStringValue(String key, Settings.SettingCategory category, String defaultValue) {
        return settingsRepository.findByKey(key)
                .map(Settings::getValue)
                .orElse(defaultValue);
    }

    private Integer getIntValue(String key, Settings.SettingCategory category, Integer defaultValue) {
        return settingsRepository.findByKey(key)
                .map(s -> {
                    try {
                        return Integer.parseInt(s.getValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private Boolean getBoolValue(String key, Settings.SettingCategory category, Boolean defaultValue) {
        return settingsRepository.findByKey(key)
                .map(s -> Boolean.parseBoolean(s.getValue()))
                .orElse(defaultValue);
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
                try {
                    return Integer.parseInt(settings.getValue());
                } catch (NumberFormatException e) {
                    return settings.getValue();
                }
            case JSON:
                try {
                    return objectMapper.readValue(settings.getValue(), Map.class);
                } catch (JsonProcessingException e) {
                    return settings.getValue();
                }
            default:
                return settings.getValue();
        }
    }
}