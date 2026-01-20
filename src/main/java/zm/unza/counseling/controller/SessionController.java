package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.SessionDto;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.SessionService;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<SessionDto>>> getAllSessions(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getAllSessions(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<SessionDto>> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getSessionById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<SessionDto>> createSession(@RequestBody SessionDto sessionDto) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.createSession(sessionDto), "Session created successfully"));
    }
}