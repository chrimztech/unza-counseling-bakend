package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.CrisisAlertResponse;
import zm.unza.counseling.entity.CrisisAlert;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.CrisisAlertRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/crisis-alerts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_COUNSELOR','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
public class CrisisAlertController {

    private final CrisisAlertRepository crisisAlertRepository;
    private final UserRepository userRepository;

    @GetMapping
    public Page<CrisisAlertResponse> listAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && !status.isBlank()) {
            CrisisAlert.AlertStatus alertStatus = CrisisAlert.AlertStatus.valueOf(status.toUpperCase());
            return crisisAlertRepository.findByStatusOrderByCreatedAtDesc(alertStatus, pageable)
                    .map(CrisisAlertResponse::from);
        }
        return crisisAlertRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(CrisisAlertResponse::from);
    }

    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> pendingCount() {
        long count = crisisAlertRepository.countByStatus(CrisisAlert.AlertStatus.PENDING);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/{id}")
    public CrisisAlertResponse getAlert(@PathVariable Long id) {
        return CrisisAlertResponse.from(
                crisisAlertRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Alert not found")));
    }

    @PatchMapping("/{id}/acknowledge")
    public CrisisAlertResponse acknowledge(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {

        CrisisAlert alert = crisisAlertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));
        User reviewer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        alert.setStatus(CrisisAlert.AlertStatus.ACKNOWLEDGED);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());
        if (body != null && body.get("notes") != null) alert.setCounselorNotes(body.get("notes"));

        return CrisisAlertResponse.from(crisisAlertRepository.save(alert));
    }

    @PatchMapping("/{id}/resolve")
    public CrisisAlertResponse resolve(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {

        CrisisAlert alert = crisisAlertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));
        User reviewer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        alert.setStatus(CrisisAlert.AlertStatus.RESOLVED);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());
        if (body != null && body.get("notes") != null) alert.setCounselorNotes(body.get("notes"));

        return CrisisAlertResponse.from(crisisAlertRepository.save(alert));
    }

    @PatchMapping("/{id}/false-positive")
    public CrisisAlertResponse markFalsePositive(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {

        CrisisAlert alert = crisisAlertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));
        User reviewer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        alert.setStatus(CrisisAlert.AlertStatus.FALSE_POSITIVE);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());
        if (body != null && body.get("notes") != null) alert.setCounselorNotes(body.get("notes"));

        return CrisisAlertResponse.from(crisisAlertRepository.save(alert));
    }
}
