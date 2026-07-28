package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.PanicButtonRequest;
import zm.unza.counseling.dto.request.SecurityAlertCreateRequest;
import zm.unza.counseling.dto.request.SecurityAlertResolveRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.SecurityAlertResponse;
import zm.unza.counseling.entity.SecurityAlert;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.SecurityAlertService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/security-alerts")
@Tag(name = "Security Alerts", description = "Sensitive-case alerts (self-harm, suicide, assault, panic button) for university Security staff")
public class SecurityAlertController {

    private final SecurityAlertService securityAlertService;
    private final UserRepository userRepository;

    public SecurityAlertController(SecurityAlertService securityAlertService, UserRepository userRepository) {
        this.securityAlertService = securityAlertService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'SUPER_ADMIN')")
    @Operation(summary = "Report a security alert", description = "Manually raise a sensitive-case alert for Security to review")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> create(
            @Valid @RequestBody SecurityAlertCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        SecurityAlert alert = securityAlertService.createManual(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SecurityAlertResponse.from(alert), "Security alert created"));
    }

    @PostMapping("/panic")
    @Operation(summary = "Panic button", description = "Any authenticated user can trigger this for themselves")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> panic(
            @RequestBody(required = false) PanicButtonRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        PanicButtonRequest body = request != null ? request : new PanicButtonRequest();
        SecurityAlert alert = securityAlertService.createPanic(body, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SecurityAlertResponse.from(alert), "Panic alert raised"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SECURITY')")
    @Operation(summary = "List security alerts", description = "Optional filters by status and/or category, newest first")
    public ResponseEntity<ApiResponse<List<SecurityAlertResponse>>> list(
            @RequestParam(required = false) SecurityAlert.Status status,
            @RequestParam(required = false) SecurityAlert.Category category) {
        List<SecurityAlertResponse> alerts = securityAlertService.list(status, category).stream()
                .map(SecurityAlertResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SECURITY')")
    @Operation(summary = "Acknowledge a security alert")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> acknowledge(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actorName = resolveDisplayName(userDetails);
        SecurityAlert alert = securityAlertService.acknowledge(id, actorName);
        return ResponseEntity.ok(ApiResponse.success(SecurityAlertResponse.from(alert), "Security alert acknowledged"));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SECURITY')")
    @Operation(summary = "Resolve a security alert")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> resolve(
            @PathVariable Long id,
            @RequestBody(required = false) SecurityAlertResolveRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SecurityAlertResolveRequest body = request != null ? request : new SecurityAlertResolveRequest();
        String actorName = resolveDisplayName(userDetails);
        boolean falsePositive = Boolean.TRUE.equals(body.getFalsePositive());
        SecurityAlert alert = securityAlertService.resolve(id, body.getResolutionNotes(), falsePositive, actorName);
        return ResponseEntity.ok(ApiResponse.success(SecurityAlertResponse.from(alert), "Security alert resolved"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        String name = userDetails.getUsername();
        return userRepository.findByUsername(name)
                .or(() -> userRepository.findByEmail(name))
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + name));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return resolveUser(userDetails).getId();
    }

    private String resolveDisplayName(UserDetails userDetails) {
        return resolveUser(userDetails).getFullName();
    }
}
