package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.service.ai.AiOversightService;

import java.util.Map;

/** Admin "AI Oversight" dashboard data: usage, reply times, reliability, detector outcomes. */
@RestController
@RequestMapping("/admin/ai-oversight")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AiOversightController {

    private final AiOversightService oversight;

    @GetMapping
    public Map<String, Object> overview(@RequestParam(defaultValue = "30") int days) {
        return oversight.overview(days);
    }
}
