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
@RequestMapping("/sessions")
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<SessionDto>> updateSession(@PathVariable Long id, @RequestBody SessionDto sessionDto) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.updateSession(id, sessionDto), "Session updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Session deleted successfully"));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<SessionDto>>> getSessionsByClient(@PathVariable Long clientId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getSessionsByClient(clientId, pageable)));
    }
}