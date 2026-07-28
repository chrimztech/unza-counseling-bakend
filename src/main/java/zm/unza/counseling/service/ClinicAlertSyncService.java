package zm.unza.counseling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import zm.unza.counseling.config.ClinicProperties;
import zm.unza.counseling.entity.SecurityAlert;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pushes SecurityAlert create/status-update events out to the university clinic system
 * so alerts raised on either side are visible on both.
 *
 * Best-effort only: every method swallows and logs exceptions so a clinic-system outage
 * never breaks a local security-alert operation.
 */
@Service
@Slf4j
public class ClinicAlertSyncService {

    private static final String CREATE_PATH = "/api/external/counseling/security-alerts/inbound";
    private static final String STATUS_PATH = "/api/external/counseling/security-alerts/inbound/%s/status";

    private final RestTemplate restTemplate;
    private final ClinicProperties clinicProperties;

    @Value("${app.cross-system.api-key:}")
    private String crossSystemApiKey;

    public ClinicAlertSyncService(RestTemplate restTemplate, ClinicProperties clinicProperties) {
        this.restTemplate = restTemplate;
        this.clinicProperties = clinicProperties;
    }

    /**
     * Notify the clinic system that a new alert was raised here. On success, stores the
     * clinic's own local id for this alert back onto {@code alert} (externalAlertId/externalSystem)
     * — callers are responsible for persisting the entity afterwards.
     */
    public void syncCreate(SecurityAlert alert) {
        if (!clinicProperties.isConfigured()) {
            log.debug("Clinic system URL not configured — skipping outbound sync for alert {}", alert.getId());
            return;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("externalAlertId", alert.getId() != null ? String.valueOf(alert.getId()) : null);
            body.put("category", alert.getCategory() != null ? alert.getCategory().name() : null);
            body.put("severity", alert.getSeverity() != null ? alert.getSeverity().name() : null);
            body.put("sourceType", alert.getSourceType() != null ? alert.getSourceType().name() : null);
            body.put("subjectStudentId", alert.getSubjectStudentId());
            body.put("subjectName", alert.getSubjectName());
            body.put("reportedByName", alert.getReportedByName());
            body.put("description", alert.getDescription());
            body.put("latitude", alert.getLatitude());
            body.put("longitude", alert.getLongitude());
            body.put("occurredAt", alert.getOccurredAt() != null
                    ? alert.getOccurredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, jsonHeaders());
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    clinicProperties.getBaseUrl() + CREATE_PATH, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object localAlertId = response.getBody().get("localAlertId");
                if (localAlertId != null) {
                    alert.setExternalAlertId(String.valueOf(localAlertId));
                    alert.setExternalSystem(SecurityAlert.ExternalSystem.CLINIC);
                }
            } else {
                log.warn("Clinic system returned non-success status {} while syncing alert {}",
                        response.getStatusCode(), alert.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to sync security alert {} to clinic system: {}", alert.getId(), e.getMessage());
        }
    }

    /**
     * Push a status change (acknowledge/resolve) to the clinic system for an alert that was
     * previously synced there (i.e. {@code externalAlertId} is set).
     */
    public void syncStatusUpdate(SecurityAlert alert) {
        if (!clinicProperties.isConfigured()) {
            return;
        }
        if (alert.getExternalAlertId() == null || alert.getExternalAlertId().isBlank()) {
            return;
        }
        try {
            String actorName = switch (alert.getStatus()) {
                case RESOLVED, FALSE_POSITIVE -> alert.getResolvedByName();
                case ACKNOWLEDGED -> alert.getAcknowledgedByName();
                default -> null;
            };

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", alert.getStatus().name());
            body.put("actorName", actorName);
            body.put("resolutionNotes", alert.getResolutionNotes());

            String url = clinicProperties.getBaseUrl() + String.format(STATUS_PATH, alert.getExternalAlertId());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, jsonHeaders());
            restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
        } catch (Exception e) {
            log.warn("Failed to sync status update for security alert {} (external id {}) to clinic system: {}",
                    alert.getId(), alert.getExternalAlertId(), e.getMessage());
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (crossSystemApiKey != null && !crossSystemApiKey.isBlank()) {
            headers.set("X-Service-Api-Key", crossSystemApiKey);
        }
        return headers;
    }
}
