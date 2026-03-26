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
import zm.unza.counseling.dto.settings.ThemeSettingsDTO;
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
    
    // Session timeout setting keys
    private static final String SEC_SESSION_TIMEOUT = "sessionTimeoutMinutes";
    private static final String SEC_SESSION_TIMEOUT_ENABLED = "sessionTimeoutEnabled";
    private static final String SEC_SESSION_WARNING = "sessionWarningMinutes";

    // Theme/Appearance setting keys
    private static final String THEME_MODE = "themeMode";
    private static final String THEME_PRIMARY_COLOR = "primaryColor";
    private static final String THEME_COMPACT_MODE = "compactMode";
    private static final String THEME_REDUCED_MOTION = "reducedMotion";
    private static final String THEME_HIGH_CONTRAST = "highContrast";
    private static final String THEME_FONT_SIZE = "fontSize";

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
        dto.setAppearance(getThemeSettings());
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
        dto.setSessionTimeoutMinutes(getIntValue(SEC_SESSION_TIMEOUT, Settings.SettingCategory.SECURITY, 30));
        dto.setSessionTimeoutEnabled(getBoolValue(SEC_SESSION_TIMEOUT_ENABLED, Settings.SettingCategory.SECURITY, true));
        dto.setSessionWarningMinutes(getIntValue(SEC_SESSION_WARNING, Settings.SettingCategory.SECURITY, 5));
        return dto;
    }

    @Override
    @Transactional
    public SecuritySettingsDTO updateSecuritySettings(SecuritySettingsDTO dto) {
        saveSetting(SEC_RETENTION, String.valueOf(dto.getDataRetentionPeriod()), Settings.SettingType.INTEGER, Settings.SettingCategory.SECURITY);
        saveSetting(SEC_ENCRYPTION, String.valueOf(dto.getEncryptionEnabled()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.SECURITY);
        saveSetting(SEC_AUDIT, String.valueOf(dto.getAuditLoggingEnabled()), Settings.SettingType.BOOLEAN, Settings.SettingCategory.SECURITY);
        saveSetting(SEC_SESSION_TIMEOUT, String.valueOf(dto.getSessionTimeoutMinutes() != null ? dto.getSessionTimeoutMinutes() : 30), Settings.SettingType.INTEGER, Settings.SettingCategory.SECURITY);
        saveSetting(SEC_SESSION_TIMEOUT_ENABLED, String.valueOf(dto.getSessionTimeoutEnabled() != null ? dto.getSessionTimeoutEnabled() : true), Settings.SettingType.BOOLEAN, Settings.SettingCategory.SECURITY);
        saveSetting(SEC_SESSION_WARNING, String.valueOf(dto.getSessionWarningMinutes() != null ? dto.getSessionWarningMinutes() : 5), Settings.SettingType.INTEGER, Settings.SettingCategory.SECURITY);
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
    @Transactional
    public boolean healthCheck() {
        try {
            // Simple health check - try to fetch count of settings
            long count = settingsRepository.count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public AllSettingsDTO updateAllSettings(AllSettingsDTO allSettings) {
        if (allSettings.getOrganization() != null) {
            updateOrganizationSettings(allSettings.getOrganization());
        }
        if (allSettings.getAppointments() != null) {
            updateAppointmentSettings(allSettings.getAppointments());
        }
        if (allSettings.getNotifications() != null) {
            updateNotificationSettings(allSettings.getNotifications());
        }
        if (allSettings.getSecurity() != null) {
            updateSecuritySettings(allSettings.getSecurity());
        }
        return getAllSettingsDTO();
    }

    @Override
    @Transactional
    public Object updateSettingByCategoryAndKey(Settings.SettingCategory category, String key, Object value) {
        switch (category) {
            case ORGANIZATION:
                return updateOrganizationSettingByKey(key, value);
            case APPOINTMENTS:
                return updateAppointmentSettingByKey(key, value);
            case NOTIFICATIONS:
                return updateNotificationSettingByKey(key, value);
            case SECURITY:
                return updateSecuritySettingByKey(key, value);
            case APPEARANCE:
                return updateThemeSettingByKey(key, value);
            default:
                throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    @Override
    @Transactional
    public SecuritySettingsDTO updateSecuritySettingByKey(String key, Object value) {
        String strValue = value != null ? value.toString() : "false";
        
        switch (key) {
            case SEC_RETENTION:
                saveSetting(SEC_RETENTION, strValue, Settings.SettingType.INTEGER, Settings.SettingCategory.SECURITY);
                break;
            case SEC_ENCRYPTION:
                saveSetting(SEC_ENCRYPTION, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.SECURITY);
                break;
            case SEC_AUDIT:
                saveSetting(SEC_AUDIT, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.SECURITY);
                break;
            default:
                throw new IllegalArgumentException("Invalid security setting key: " + key);
        }
        return getSecuritySettings();
    }

    @Override
    @Transactional
    public OrganizationSettingsDTO updateOrganizationSettingByKey(String key, Object value) {
        String strValue = value != null ? value.toString() : "";
        
        switch (key) {
            case "orgName":
            case ORG_NAME:
                saveSetting(ORG_NAME, strValue, Settings.SettingType.STRING, Settings.SettingCategory.ORGANIZATION);
                break;
            case "email":
            case ORG_EMAIL:
                saveSetting(ORG_EMAIL, strValue, Settings.SettingType.STRING, Settings.SettingCategory.ORGANIZATION);
                break;
            case "phone":
            case ORG_PHONE:
                saveSetting(ORG_PHONE, strValue, Settings.SettingType.STRING, Settings.SettingCategory.ORGANIZATION);
                break;
            default:
                throw new IllegalArgumentException("Invalid organization setting key: " + key);
        }
        return getOrganizationSettings();
    }

    @Override
    @Transactional
    public NotificationSettingsDTO updateNotificationSettingByKey(String key, Object value) {
        String strValue = value != null ? value.toString() : "false";
        
        switch (key) {
            case NOTIF_APPT_REMINDERS:
                saveSetting(NOTIF_APPT_REMINDERS, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
                break;
            case NOTIF_FOLLOW_UP:
                saveSetting(NOTIF_FOLLOW_UP, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
                break;
            case NOTIF_WEEKLY:
                saveSetting(NOTIF_WEEKLY, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
                break;
            case NOTIF_EMAIL:
                saveSetting(NOTIF_EMAIL, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
                break;
            case NOTIF_SMS:
                saveSetting(NOTIF_SMS, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.NOTIFICATIONS);
                break;
            default:
                throw new IllegalArgumentException("Invalid notification setting key: " + key);
        }
        return getNotificationSettings();
    }

    @Override
    @Transactional
    public AppointmentSettingsDTO updateAppointmentSettingByKey(String key, Object value) {
        String strValue = value != null ? value.toString() : "0";
        
        switch (key) {
            case APPT_DURATION:
                saveSetting(APPT_DURATION, strValue, Settings.SettingType.INTEGER, Settings.SettingCategory.APPOINTMENTS);
                break;
            case APPT_MAX_DAYS:
                saveSetting(APPT_MAX_DAYS, strValue, Settings.SettingType.INTEGER, Settings.SettingCategory.APPOINTMENTS);
                break;
            case APPT_CANCEL_HOURS:
                saveSetting(APPT_CANCEL_HOURS, strValue, Settings.SettingType.INTEGER, Settings.SettingCategory.APPOINTMENTS);
                break;
            case APPT_AUTO_CONFIRM:
                saveSetting(APPT_AUTO_CONFIRM, strValue, Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPOINTMENTS);
                break;
            default:
                throw new IllegalArgumentException("Invalid appointment setting key: " + key);
        }
        return getAppointmentSettings();
    }

    @Override
    @Transactional(readOnly = true)
    public ThemeSettingsDTO getThemeSettings() {
        ThemeSettingsDTO dto = new ThemeSettingsDTO();
        dto.setThemeMode(getStringValue(THEME_MODE, Settings.SettingCategory.APPEARANCE, "LIGHT"));
        dto.setPrimaryColor(getStringValue(THEME_PRIMARY_COLOR, Settings.SettingCategory.APPEARANCE, "#3B82F6"));
        dto.setCompactMode(getBoolValue(THEME_COMPACT_MODE, Settings.SettingCategory.APPEARANCE, false));
        dto.setReducedMotion(getBoolValue(THEME_REDUCED_MOTION, Settings.SettingCategory.APPEARANCE, false));
        dto.setHighContrast(getBoolValue(THEME_HIGH_CONTRAST, Settings.SettingCategory.APPEARANCE, false));
        dto.setFontSize(getStringValue(THEME_FONT_SIZE, Settings.SettingCategory.APPEARANCE, "MEDIUM"));
        return dto;
    }

    @Override
    @Transactional
    public ThemeSettingsDTO updateThemeSettings(ThemeSettingsDTO dto) {
        saveSetting(THEME_MODE, dto.getThemeMode() != null ? dto.getThemeMode() : "LIGHT", 
                Settings.SettingType.STRING, Settings.SettingCategory.APPEARANCE);
        saveSetting(THEME_PRIMARY_COLOR, dto.getPrimaryColor() != null ? dto.getPrimaryColor() : "#3B82F6", 
                Settings.SettingType.STRING, Settings.SettingCategory.APPEARANCE);
        saveSetting(THEME_COMPACT_MODE, String.valueOf(dto.getCompactMode() != null ? dto.getCompactMode() : false), 
                Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPEARANCE);
        saveSetting(THEME_REDUCED_MOTION, String.valueOf(dto.getReducedMotion() != null ? dto.getReducedMotion() : false), 
                Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPEARANCE);
        saveSetting(THEME_HIGH_CONTRAST, String.valueOf(dto.getHighContrast() != null ? dto.getHighContrast() : false), 
                Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPEARANCE);
        saveSetting(THEME_FONT_SIZE, dto.getFontSize() != null ? dto.getFontSize() : "MEDIUM", 
                Settings.SettingType.STRING, Settings.SettingCategory.APPEARANCE);
        return getThemeSettings();
    }

    @Override
    @Transactional
    public ThemeSettingsDTO updateThemeSettingByKey(String key, Object value) {
        String strValue = value != null ? value.toString() : "";
        
        switch (key) {
            case THEME_MODE:
            case "themeMode":
            case "mode":
                saveSetting(THEME_MODE, strValue.isEmpty() ? "LIGHT" : strValue, 
                        Settings.SettingType.STRING, Settings.SettingCategory.APPEARANCE);
                break;
            case THEME_PRIMARY_COLOR:
            case "primaryColor":
            case "color":
                saveSetting(THEME_PRIMARY_COLOR, strValue.isEmpty() ? "#3B82F6" : strValue, 
                        Settings.SettingType.STRING, Settings.SettingCategory.APPEARANCE);
                break;
            case THEME_COMPACT_MODE:
            case "compactMode":
            case "compact":
                saveSetting(THEME_COMPACT_MODE, strValue.isEmpty() ? "false" : strValue, 
                        Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPEARANCE);
                break;
            case THEME_REDUCED_MOTION:
            case "reducedMotion":
            case "reduced":
                saveSetting(THEME_REDUCED_MOTION, strValue.isEmpty() ? "false" : strValue, 
                        Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPEARANCE);
                break;
            case THEME_HIGH_CONTRAST:
            case "highContrast":
            case "contrast":
                saveSetting(THEME_HIGH_CONTRAST, strValue.isEmpty() ? "false" : strValue, 
                        Settings.SettingType.BOOLEAN, Settings.SettingCategory.APPEARANCE);
                break;
            case THEME_FONT_SIZE:
            case "fontSize":
            case "font":
                saveSetting(THEME_FONT_SIZE, strValue.isEmpty() ? "MEDIUM" : strValue, 
                        Settings.SettingType.STRING, Settings.SettingCategory.APPEARANCE);
                break;
            default:
                throw new IllegalArgumentException("Invalid theme setting key: " + key);
        }
        return getThemeSettings();
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