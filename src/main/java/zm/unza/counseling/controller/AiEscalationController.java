package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.entity.CrisisAlert;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.ChatMessageRepository;
import zm.unza.counseling.repository.CrisisAlertRepository;
import zm.unza.counseling.repository.MessageRepository;
import zm.unza.counseling.service.ai.CrisisExplanationService;
import zm.unza.counseling.service.ai.CrisisExplanationService.Source;

import java.util.*;

/**
 * The counsellor escalation queue: each crisis alert with the flagged text and a word-level
 * explanation of why it was flagged. Status changes reuse {@link CrisisAlertController}
 * (acknowledge / resolve / false-positive).
 */
@RestController
@RequestMapping("/crisis-alerts/queue")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COUNSELOR','ADMIN','SUPER_ADMIN')")
@Transactional(readOnly = true)
public class AiEscalationController {

    static final String AI_CHAT_TAG = "[AI wellness chat]";

    private final CrisisAlertRepository alerts;
    private final ChatMessageRepository chatMessages;
    private final MessageRepository messages;
    private final AppointmentRepository appointments;
    private final CrisisExplanationService explainer;

    @GetMapping
    public Map<String, Object> queue(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(required = false) String status) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CrisisAlert> p = status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)
                ? alerts.findAllByOrderByCreatedAtDesc(pageable)
                : alerts.findByStatusOrderByCreatedAtDesc(CrisisAlert.AlertStatus.valueOf(status.toUpperCase(Locale.ROOT)), pageable);

        Map<String, Object> counts = new LinkedHashMap<>();
        for (CrisisAlert.AlertStatus s : CrisisAlert.AlertStatus.values()) counts.put(s.name(), alerts.countByStatus(s));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", p.getContent().stream().map(this::toItem).toList());
        out.put("page", p.getNumber());
        out.put("totalPages", p.getTotalPages());
        out.put("totalElements", p.getTotalElements());
        out.put("counts", counts);
        return out;
    }

    @GetMapping("/{id}")
    public Map<String, Object> one(@PathVariable Long id) {
        return toItem(alerts.findById(id).orElseThrow(() -> new NoSuchElementException("Alert not found")));
    }

    private Map<String, Object> toItem(CrisisAlert a) {
        String raw = a.getTriggeredKeywords() == null ? "" : a.getTriggeredKeywords();
        boolean aiChat = raw.startsWith(AI_CHAT_TAG);
        Source source = aiChat ? Source.AI_CHAT
                : a.getSourceType() == CrisisAlert.SourceType.APPOINTMENT ? Source.APPOINTMENT : Source.MESSAGE;
        List<String> keywords = Arrays.stream(raw.replace(AI_CHAT_TAG, "").split(","))
                .map(String::strip).filter(k -> !k.isEmpty()).toList();

        String text = a.getSourceExcerpt() != null ? a.getSourceExcerpt() : lookupText(a, source);
        CrisisExplanationService.Explanation ex = text != null
                ? explainer.explain(text, source)
                : explainer.fromKeywords(keywords, a.getSeverity().name());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("severity", a.getSeverity().name());
        m.put("status", a.getStatus().name());
        m.put("source", switch (source) {
            case AI_CHAT -> "AI wellness chat";
            case MESSAGE -> "Direct message";
            case APPOINTMENT -> "Appointment request";
        });
        m.put("sourceId", a.getSourceId());
        m.put("createdAt", a.getCreatedAt());
        if (a.getClient() != null) {
            m.put("clientId", a.getClient().getId());
            m.put("clientName", (Objects.toString(a.getClient().getFirstName(), "") + " "
                    + Objects.toString(a.getClient().getLastName(), "")).strip());
        }
        m.put("triggeredKeywords", keywords);
        m.put("counselorNotes", a.getCounselorNotes());
        if (a.getReviewedBy() != null) {
            m.put("reviewedByName", (Objects.toString(a.getReviewedBy().getFirstName(), "") + " "
                    + Objects.toString(a.getReviewedBy().getLastName(), "")).strip());
        }
        m.put("reviewedAt", a.getReviewedAt());
        m.put("explanation", ex);
        return m;
    }

    private String lookupText(CrisisAlert a, Source source) {
        if (a.getSourceId() == null) return null;
        try {
            return switch (source) {
                case AI_CHAT -> chatMessages.findById(a.getSourceId()).map(c -> c.getMessage()).orElse(null);
                case MESSAGE -> messages.findById(a.getSourceId())
                        .map(msg -> CrisisAlert.excerpt(msg.getSubject(), msg.getContent())).orElse(null);
                case APPOINTMENT -> appointments.findById(a.getSourceId())
                        .map(ap -> CrisisAlert.excerpt(ap.getTitle(), ap.getDescription(), ap.getPresentingConcern())).orElse(null);
            };
        } catch (Exception e) {
            return null;
        }
    }
}
