package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.AiProviderConfig;
import zm.unza.counseling.entity.ChatMessage;
import zm.unza.counseling.entity.CrisisAlert;
import zm.unza.counseling.repository.AiProviderConfigRepository;
import zm.unza.counseling.repository.ChatMessageRepository;
import zm.unza.counseling.repository.CrisisAlertRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Figures for the admin "AI Oversight" dashboard, computed only from what the system recorded:
 * per-message metrics on chat_messages, provider usage counters, and crisis alerts with the
 * counsellors' review outcomes. Contains no message text.
 */
@Service
@RequiredArgsConstructor
public class AiOversightService {

    private final ChatMessageRepository chats;
    private final CrisisAlertRepository alerts;
    private final AiProviderConfigRepository providers;

    @Transactional(readOnly = true)
    public Map<String, Object> overview(int days) {
        int window = Math.min(Math.max(days, 1), 365);
        LocalDate today = LocalDate.now();
        LocalDateTime since = today.minusDays(window - 1L).atStartOfDay();

        List<ChatMessage> rows = chats.findByCreatedAtGreaterThanEqual(since);
        List<CrisisAlert> alertRows = alerts.findByCreatedAtGreaterThanEqual(since);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("days", window);
        out.put("since", since);
        out.put("generatedAt", LocalDateTime.now());
        out.put("usage", usage(rows, today, window));
        out.put("latency", latency(rows));
        out.put("reliability", reliability(rows));
        out.put("knowledge", knowledge(rows));
        out.put("detector", detector(rows));
        out.put("escalations", escalations(alertRows));
        out.put("providers", providerList());
        return out;
    }

    private Map<String, Object> usage(List<ChatMessage> rows, LocalDate today, int window) {
        Map<LocalDate, List<ChatMessage>> byDay = rows.stream().filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate()));
        List<Map<String, Object>> perDay = new ArrayList<>();
        for (int i = window - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            List<ChatMessage> list = byDay.getOrDefault(d, List.of());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", d.toString());
            m.put("messages", list.size());
            m.put("students", list.stream().map(ChatMessage::getUserId).filter(Objects::nonNull).distinct().count());
            perDay.add(m);
        }
        Map<String, Object> u = new LinkedHashMap<>();
        u.put("messages", rows.size());
        u.put("students", rows.stream().map(ChatMessage::getUserId).filter(Objects::nonNull).distinct().count());
        u.put("conversations", rows.stream().map(r -> r.getUserId() + "/" + r.getSessionId()).distinct().count());
        u.put("perDay", perDay);
        return u;
    }

    private Map<String, Object> latency(List<ChatMessage> rows) {
        // Only replies the AI produced: the canned offline reply is instant and would flatter the figures.
        List<Long> ms = rows.stream().filter(r -> !Boolean.FALSE.equals(r.getAiAnswered()))
                .map(ChatMessage::getReplyMs).filter(Objects::nonNull).sorted().toList();
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("measured", ms.size());
        l.put("p50Ms", percentile(ms, 50));
        l.put("p95Ms", percentile(ms, 95));
        l.put("maxMs", ms.isEmpty() ? null : ms.get(ms.size() - 1));
        l.put("meanMs", ms.isEmpty() ? null : Math.round(ms.stream().mapToLong(Long::longValue).average().orElse(0)));
        return l;
    }

    private Map<String, Object> reliability(List<ChatMessage> rows) {
        List<ChatMessage> measured = rows.stream().filter(r -> r.getAiAnswered() != null).toList();
        long offline = measured.stream().filter(r -> !r.getAiAnswered()).count();
        long fallback = measured.stream().filter(r -> r.getProviderAttempt() != null && r.getProviderAttempt() > 1).count();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("measured", measured.size());
        r.put("unanswered", offline);
        r.put("answeredByFallback", fallback);
        r.put("unansweredRate", rate(offline, measured.size()));
        r.put("fallbackRate", rate(fallback, measured.size()));
        Map<String, Long> byProvider = measured.stream().filter(x -> x.getProviderName() != null)
                .collect(Collectors.groupingBy(ChatMessage::getProviderName, TreeMap::new, Collectors.counting()));
        r.put("answeredByProvider", byProvider);
        return r;
    }

    private Map<String, Object> knowledge(List<ChatMessage> rows) {
        List<ChatMessage> measured = rows.stream().filter(r -> r.getMatchedScenarios() != null).toList();
        long matched = measured.stream().filter(r -> r.getMatchedScenarios() > 0).count();
        Map<String, Object> k = new LinkedHashMap<>();
        k.put("measured", measured.size());
        k.put("withMatch", matched);
        k.put("matchRate", rate(matched, measured.size()));
        k.put("avgScenariosWhenMatched", measured.stream().filter(r -> r.getMatchedScenarios() > 0)
                .mapToInt(ChatMessage::getMatchedScenarios).average().stream().map(d -> Math.round(d * 10) / 10.0).boxed().findFirst().orElse(null));
        return k;
    }

    private Map<String, Object> detector(List<ChatMessage> rows) {
        List<ChatMessage> measured = rows.stream().filter(r -> r.getCrisisLevel() != null).toList();
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("measured", measured.size());
        for (String lvl : List.of("NONE", "HIGH", "CRITICAL")) {
            d.put(lvl, measured.stream().filter(r -> lvl.equals(r.getCrisisLevel())).count());
        }
        long flagged = measured.stream().filter(r -> !"NONE".equals(r.getCrisisLevel())).count();
        d.put("flagRate", rate(flagged, measured.size()));
        return d;
    }

    private Map<String, Object> escalations(List<CrisisAlert> list) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("total", list.size());
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (CrisisAlert.Severity s : CrisisAlert.Severity.values())
            bySeverity.put(s.name(), list.stream().filter(a -> a.getSeverity() == s).count());
        e.put("bySeverity", bySeverity);
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (CrisisAlert.AlertStatus s : CrisisAlert.AlertStatus.values())
            byStatus.put(s.name(), list.stream().filter(a -> a.getStatus() == s).count());
        e.put("byStatus", byStatus);

        Map<String, Long> bySource = new LinkedHashMap<>();
        bySource.put("AI wellness chat", list.stream().filter(AiOversightService::isAiChat).count());
        bySource.put("Direct message", list.stream().filter(a -> !isAiChat(a) && a.getSourceType() == CrisisAlert.SourceType.MESSAGE).count());
        bySource.put("Appointment request", list.stream().filter(a -> a.getSourceType() == CrisisAlert.SourceType.APPOINTMENT).count());
        e.put("bySource", bySource);

        // Outcome matrix: severity x status, for the stacked chart.
        List<Map<String, Object>> matrix = new ArrayList<>();
        for (CrisisAlert.Severity s : CrisisAlert.Severity.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("severity", s.name());
            for (CrisisAlert.AlertStatus st : CrisisAlert.AlertStatus.values())
                row.put(st.name(), list.stream().filter(a -> a.getSeverity() == s && a.getStatus() == st).count());
            matrix.add(row);
        }
        e.put("outcomes", matrix);

        List<CrisisAlert> reviewed = list.stream().filter(a -> a.getStatus() != CrisisAlert.AlertStatus.PENDING).toList();
        long fp = reviewed.stream().filter(a -> a.getStatus() == CrisisAlert.AlertStatus.FALSE_POSITIVE).count();
        e.put("reviewed", reviewed.size());
        e.put("falsePositives", fp);
        // Precision proxy: of the alerts a counsellor has reviewed, the share they marked false positive.
        e.put("falsePositiveRate", rate(fp, reviewed.size()));
        List<Long> minutes = reviewed.stream().filter(a -> a.getReviewedAt() != null && a.getCreatedAt() != null)
                .map(a -> Duration.between(a.getCreatedAt(), a.getReviewedAt()).toMinutes()).sorted().toList();
        e.put("medianMinutesToReview", percentile(minutes, 50));
        return e;
    }

    private List<Map<String, Object>> providerList() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (AiProviderConfig p : providers.findAllByOrderByIsDefaultDescPriorityAscIdAsc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("type", p.getProviderType() == null ? null : p.getProviderType().getDisplayName());
            m.put("model", p.getModel());
            m.put("enabled", p.getEnabled());
            m.put("isDefault", p.getIsDefault());
            long total = p.getTotalRequests() == null ? 0 : p.getTotalRequests();
            long failed = p.getFailedRequests() == null ? 0 : p.getFailedRequests();
            m.put("totalRequests", total);
            m.put("failedRequests", failed);
            m.put("failureRate", rate(failed, total));
            m.put("lastUsedAt", p.getLastUsedAt());
            m.put("lastError", p.getLastError());
            out.add(m);
        }
        return out;
    }

    private static boolean isAiChat(CrisisAlert a) {
        return a.getTriggeredKeywords() != null && a.getTriggeredKeywords().startsWith("[AI wellness chat]");
    }

    private static Double rate(long part, long whole) {
        return whole == 0 ? null : Math.round(part * 1000.0 / whole) / 10.0;   // percent, 1 dp
    }

    /** Nearest-rank percentile of a sorted list. */
    static Long percentile(List<Long> sorted, int pct) {
        if (sorted.isEmpty()) return null;
        int rank = (int) Math.ceil(pct / 100.0 * sorted.size());
        return sorted.get(Math.min(Math.max(rank, 1), sorted.size()) - 1);
    }
}
