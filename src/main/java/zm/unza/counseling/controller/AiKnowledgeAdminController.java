package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.AiAssistantSettings;
import zm.unza.counseling.entity.AiCategoryGuidance;
import zm.unza.counseling.entity.AiCounsellingScenario;
import zm.unza.counseling.service.ai.*;

import java.util.*;

/**
 * Super-admin management of what the wellness assistant knows: the counsellor dialogue dataset
 * (upload the AI Counselling Dialogue Template .xlsx), category guidance, crisis contacts,
 * campus services and extra instructions — plus a preview to see retrieval and replies.
 */
@RestController
@RequestMapping({"/api/v1/admin/ai-knowledge", "/api/admin/ai-knowledge", "/v1/admin/ai-knowledge", "/admin/ai-knowledge"})
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AiKnowledgeAdminController {

    private final AiKnowledgeService knowledge;
    private final AiGatewayService gateway;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        Map<String, Object> out = new LinkedHashMap<>(knowledge.stats());
        AiAssistantSettings s = knowledge.settings();
        out.put("lastImportAt", s.getLastImportAt());
        out.put("lastImportFile", s.getLastImportFile());
        out.put("lastImportSummary", s.getLastImportSummary());
        out.put("aiAvailable", gateway.isAvailable());
        return ResponseEntity.ok(ApiResponse.success(out));
    }

    /** Upload the dialogue workbook. {@code replace=true} (default) swaps out previously imported scenarios; hand-added ones are kept. */
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importWorkbook(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "replace", defaultValue = "true") boolean replace,
            Authentication auth) {
        Map<String, Object> result = knowledge.importWorkbook(file, replace, auth.getName());
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("summary")));
    }

    @GetMapping("/scenarios")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scenarios(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String risk,
            @RequestParam(required = false) Boolean hasResponse,
            @RequestParam(required = false) Boolean aiDrafted,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Page<AiCounsellingScenario> p = knowledge.search(category, risk, hasResponse, aiDrafted, q, page, size);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", p.getContent());
        out.put("totalElements", p.getTotalElements());
        out.put("totalPages", p.getTotalPages());
        out.put("page", p.getNumber());
        out.put("size", p.getSize());
        return ResponseEntity.ok(ApiResponse.success(out));
    }

    @PostMapping("/scenarios")
    public ResponseEntity<ApiResponse<AiCounsellingScenario>> create(@RequestBody Map<String, Object> body, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(knowledge.saveScenario(null, body, auth.getName()), "Scenario added"));
    }

    @PutMapping("/scenarios/{id}")
    public ResponseEntity<ApiResponse<AiCounsellingScenario>> update(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(knowledge.saveScenario(id, body, auth.getName()), "Scenario saved"));
    }

    @DeleteMapping("/scenarios/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        knowledge.deleteScenario(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Scenario deleted"));
    }

    @GetMapping("/guidance")
    public ResponseEntity<ApiResponse<List<AiCategoryGuidance>>> guidance() {
        return ResponseEntity.ok(ApiResponse.success(knowledge.guidance()));
    }

    @PutMapping("/guidance")
    public ResponseEntity<ApiResponse<List<AiCategoryGuidance>>> replaceGuidance(@RequestBody List<Map<String, Object>> rows) {
        return ResponseEntity.ok(ApiResponse.success(knowledge.replaceGuidance(rows), "Guidance saved"));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<AiAssistantSettings>> settings() {
        return ResponseEntity.ok(ApiResponse.success(knowledge.settings()));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<AiAssistantSettings>> updateSettings(@RequestBody Map<String, Object> body, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(knowledge.updateSettings(body, auth.getName()), "Settings saved"));
    }

    /**
     * Shows which counsellor scenarios a message retrieves and the full system prompt; with
     * {@code generate=true} also runs it through the configured providers. Nothing is stored.
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> preview(@RequestBody Map<String, Object> body) {
        String message = Objects.toString(body.get("message"), "").trim();
        if (message.isEmpty()) return ResponseEntity.badRequest().body(ApiResponse.error("Type a student message to preview"));
        boolean generate = Boolean.TRUE.equals(body.get("generate"));

        AiKnowledgeService.WellnessPrompt prompt = knowledge.buildWellnessSystemPrompt(message, null);
        List<Map<String, Object>> examples = new ArrayList<>();
        for (AiKnowledgeService.RetrievedExample e : prompt.examples()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.scenario().getId());
            m.put("score", Math.round(e.score() * 100) / 100.0);
            m.put("problemCategory", e.scenario().getProblemCategory());
            m.put("riskLevel", e.scenario().getRiskLevel());
            m.put("studentMessage", e.scenario().getStudentMessage());
            m.put("idealResponse", e.scenario().getIdealResponse());
            examples.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("examples", examples);
        out.put("systemPrompt", prompt.systemPrompt());
        if (generate) {
            try {
                AiResult r = gateway.chat(prompt.systemPrompt(), List.of(AiMessage.user(message)));
                out.put("reply", r.content());
                out.put("provider", r.providerName() + " · " + r.model());
                out.put("latencyMs", r.latencyMs());
            } catch (AiUnavailableException e) {
                out.put("error", e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success(out));
    }
}
