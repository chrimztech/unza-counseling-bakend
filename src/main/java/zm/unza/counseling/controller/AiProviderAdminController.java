package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.AiProviderConfigRequest;
import zm.unza.counseling.dto.response.AiProviderConfigResponse;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.AiProviderType;
import zm.unza.counseling.service.ai.AiProviderAdminService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Super-admin management of LLM provider connections (keys, models, default, fallback order).
 * Every endpoint is SUPER_ADMIN only — plain ADMINs cannot see or change API keys.
 */
@RestController
@RequestMapping({"/api/v1/admin/ai-providers", "/api/admin/ai-providers", "/v1/admin/ai-providers", "/admin/ai-providers"})
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AiProviderAdminController {

    private final AiProviderAdminService service;

    /** Provider catalogue for the add/edit form (default URLs, whether a key is needed, model hints). */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> types() {
        List<Map<String, Object>> types = Arrays.stream(AiProviderType.values()).map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", t.name());
            m.put("displayName", t.getDisplayName());
            m.put("apiStyle", t.getApiStyle().name());
            m.put("defaultBaseUrl", t.getDefaultBaseUrl());
            m.put("apiKeyRequired", t.isApiKeyRequired());
            m.put("suggestedModels", t.getSuggestedModels());
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiProviderConfigResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                service.list().stream().map(AiProviderConfigResponse::from).toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiProviderConfigResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(AiProviderConfigResponse.from(service.get(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiProviderConfigResponse>> create(
            @Valid @RequestBody AiProviderConfigRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                AiProviderConfigResponse.from(service.create(req, auth.getName())), "AI provider added"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiProviderConfigResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AiProviderConfigRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                AiProviderConfigResponse.from(service.update(id, req, auth.getName())), "AI provider updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "AI provider deleted"));
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AiProviderConfigResponse>> makeDefault(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                AiProviderConfigResponse.from(service.makeDefault(id, auth.getName())), "Default AI provider set"));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> test(@PathVariable Long id) {
        Map<String, Object> result = service.test(id);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.ok(new ApiResponse<>(ok, ok ? "Connection successful" : "Connection failed", result));
    }

    /**
     * Fetch the live model list. Body: {@code {id?, providerType?, baseUrl?, apiKey?}} — works for a
     * saved provider (by id) or for the unsaved add form. POST so a typed key never lands in a URL/log.
     */
    @PostMapping("/models")
    public ResponseEntity<ApiResponse<List<String>>> models(@RequestBody Map<String, String> body) {
        Long id = body.get("id") == null || body.get("id").isBlank() ? null : Long.valueOf(body.get("id"));
        AiProviderType type = body.get("providerType") == null || body.get("providerType").isBlank()
                ? null : AiProviderType.valueOf(body.get("providerType"));
        return ResponseEntity.ok(ApiResponse.success(
                service.models(id, type, body.get("baseUrl"), body.get("apiKey"))));
    }
}
