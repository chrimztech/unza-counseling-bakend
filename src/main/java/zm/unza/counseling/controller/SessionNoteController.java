package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.SessionNoteDto;
import zm.unza.counseling.dto.request.SessionNoteRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.SessionNoteService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/sessions", "/api/sessions", "/v1/sessions", "/sessions"})
@RequiredArgsConstructor
public class SessionNoteController {

    private final SessionNoteService sessionNoteService;

    @GetMapping("/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<SessionNoteDto>>> getAllNotes(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(sessionNoteService.getAllNotes(pageable)));
    }

    @GetMapping("/{sessionId}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<SessionNoteDto>>> getNotesBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(sessionNoteService.getNotesBySession(sessionId)));
    }

    @PostMapping("/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<SessionNoteDto>> createNote(@RequestBody SessionNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sessionNoteService.createNote(request), "Session note created successfully"));
    }

    @PutMapping("/notes/{noteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<SessionNoteDto>> updateNote(@PathVariable Long noteId, @RequestBody SessionNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sessionNoteService.updateNote(noteId, request), "Session note updated successfully"));
    }
}
