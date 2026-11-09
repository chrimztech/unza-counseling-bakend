package zm.unza.counseling.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.entity.Resource;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.repository.MentalHealthAcademicAnalysisRepository;
import zm.unza.counseling.repository.ResourceRepository;
import zm.unza.counseling.repository.SessionRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Counselor-facing AI analytics.
 *
 * Rule followed throughout: every number in a response is computed from real records here;
 * the LLM only writes the narrative (approaches, improvement areas, self-care tips, reasons a
 * resource fits), from aggregates — never from names, notes or other identifying text. If no
 * AI provider is available the computed figures are still returned with {@code aiAvailable=false}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiInsightsService {

    private static final int WINDOW_DAYS = 90;
    /** Weekly contact hours treated as a full load for the workload index. */
    private static final double FULL_LOAD_HOURS_PER_WEEK = 25.0;

    private final SessionRepository sessionRepository;
    private final MentalHealthAcademicAnalysisRepository analysisRepository;
    private final ResourceRepository resourceRepository;
    private final AiGatewayService gateway;
    private final ObjectMapper mapper;

    // =========================================================================== counselor insights

    @Transactional(readOnly = true)
    public Map<String, Object> counselorInsights(Long counselorUserId) {
        LocalDateTime since = LocalDateTime.now().minusDays(WINDOW_DAYS);
        List<Session> sessions = sessionRepository.findAllByCounselorId(counselorUserId).stream()
                .filter(s -> s.getSessionDate() != null && s.getSessionDate().isAfter(since))
                .sorted(Comparator.comparing(Session::getSessionDate))
                .toList();
        List<Session> completed = sessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.COMPLETED).toList();

        // ---- caseload
        Map<String, Long> byType = countBy(sessions, s -> s.getType() == null ? "Unspecified" : s.getType().getDisplayName());
        Map<String, Long> riskLevels = latestRiskPerClient(sessions);
        long distinctClients = sessions.stream().map(AiInsightsService::clientKey).filter(Objects::nonNull).distinct().count();

        // ---- outcomes
        Map<String, Long> outcomes = countBy(completed.stream().filter(s -> s.getOutcome() != null).toList(),
                s -> s.getOutcome().name());
        long noShows = sessions.stream().filter(s -> s.getStatus() == Session.SessionStatus.NO_SHOW).count();
        long cancelled = sessions.stream().filter(s -> s.getStatus() == Session.SessionStatus.CANCELLED).count();
        OptionalDouble avgSatisfaction = completed.stream().map(Session::getStudentSatisfactionRating)
                .filter(Objects::nonNull).mapToInt(Integer::intValue).average();
        List<String> progressTrends = progressTrends(completed, sessions.size(), noShows, cancelled, avgSatisfaction);

        // ---- workload
        double weeks = WINDOW_DAYS / 7.0;
        double hoursPerWeek = completed.stream()
                .mapToInt(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 50).sum() / 60.0 / weeks;
        double highRiskShare = riskLevels.isEmpty() ? 0 :
                (riskLevels.getOrDefault("HIGH", 0L) + riskLevels.getOrDefault("CRITICAL", 0L))
                        / (double) riskLevels.values().stream().mapToLong(Long::longValue).sum();
        int workloadIndex = (int) Math.round(Math.min(100,
                Math.min(1.0, hoursPerWeek / FULL_LOAD_HOURS_PER_WEEK) * 70 + highRiskShare * 30));
        String schedule = scheduleSummary(completed, hoursPerWeek);

        // ---- real client feedback, most recent first, shown verbatim (counselor's own clients)
        List<String> clientFeedback = new ArrayList<>();
        for (int i = completed.size() - 1; i >= 0 && clientFeedback.size() < 5; i--) {
            String f = completed.get(i).getStudentFeedback();
            if (f != null && !f.isBlank()) clientFeedback.add(f.trim());
        }

        // ---- LLM narrative from aggregates only
        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("windowDays", WINDOW_DAYS);
        facts.put("sessionsTotal", sessions.size());
        facts.put("sessionsCompleted", completed.size());
        facts.put("noShows", noShows);
        facts.put("cancelled", cancelled);
        facts.put("distinctClients", distinctClients);
        facts.put("sessionsByType", byType);
        facts.put("latestRiskLevelPerClient", riskLevels);
        facts.put("outcomesOfCompletedSessions", outcomes);
        facts.put("averageSatisfactionOutOf5", avgSatisfaction.isPresent() ? round1(avgSatisfaction.getAsDouble()) : null);
        facts.put("contactHoursPerWeek", round1(hoursPerWeek));
        facts.put("interventionsByOutcome", interventionsByOutcome(completed));

        List<String> successful = List.of();
        List<String> improve = List.of();
        List<String> selfCare = List.of();
        boolean aiAvailable = false;
        String aiError = null;
        String provider = null;
        if (completed.isEmpty()) {
            aiError = "No completed sessions in the last " + WINDOW_DAYS + " days to analyse yet.";
        } else {
            try {
                AiResult r = gateway.chat("""
                        You are a clinical supervisor assistant for the UNZA Counseling Centre. You receive \
                        aggregated, de-identified statistics about one counselor's last 90 days of sessions.
                        Base every statement strictly on the data provided; do not invent numbers or facts. \
                        If the data is too thin to support a point, say less rather than guessing.
                        Respond with ONLY a JSON object (no markdown) of this shape:
                        {"successfulApproaches": string[], "areasForImprovement": string[], "selfCareRecommendations": string[]}
                        Each list: 2–4 short items (under 20 words each).
                        """, List.of(AiMessage.user(toJson(facts))), 800);
                JsonNode json = extractJson(r.content());
                if (json != null) {
                    successful = strings(json.path("successfulApproaches"));
                    improve = strings(json.path("areasForImprovement"));
                    selfCare = strings(json.path("selfCareRecommendations"));
                    aiAvailable = true;
                    provider = r.providerName() + " · " + r.model();
                } else {
                    aiError = "The AI response could not be read. Try again.";
                }
            } catch (AiUnavailableException e) {
                aiError = e.getMessage();
            }
        }

        Map<String, Object> caseload = new LinkedHashMap<>();
        caseload.put("clientDistribution", byType);
        caseload.put("riskLevels", riskLevels);
        caseload.put("progressTrends", progressTrends);

        Map<String, Object> effectiveness = new LinkedHashMap<>();
        effectiveness.put("successfulApproaches", successful);
        effectiveness.put("areasForImprovement", improve);
        effectiveness.put("clientFeedback", clientFeedback);
        effectiveness.put("outcomes", outcomes);

        Map<String, Object> workload = new LinkedHashMap<>();
        workload.put("optimalSchedule", schedule);
        workload.put("burnoutRisk", workloadIndex);
        workload.put("burnoutRiskBasis", String.format(
                "Workload index: %.1f contact hours/week against a %.0f h/week full load (70%%) plus the share of "
                + "clients last assessed HIGH/CRITICAL risk (30%%). An indicator, not a clinical measure.",
                hoursPerWeek, FULL_LOAD_HOURS_PER_WEEK));
        workload.put("selfCareRecommendations", selfCare);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("caseloadAnalysis", caseload);
        out.put("interventionEffectiveness", effectiveness);
        out.put("workloadOptimization", workload);
        out.put("stats", facts);
        out.put("aiAvailable", aiAvailable);
        out.put("aiMessage", aiError);
        out.put("aiProvider", provider);
        out.put("generatedAt", LocalDateTime.now());
        return out;
    }

    private static Map<String, Long> latestRiskPerClient(List<Session> chronological) {
        Map<String, Session.RiskLevelAssessed> latest = new HashMap<>();
        for (Session s : chronological) {
            String key = clientKey(s);
            if (key != null && s.getRiskLevelAssessed() != null) latest.put(key, s.getRiskLevelAssessed());
        }
        Map<String, Long> out = new LinkedHashMap<>();
        for (Session.RiskLevelAssessed level : Session.RiskLevelAssessed.values()) {
            long n = latest.values().stream().filter(l -> l == level).count();
            if (n > 0) out.put(level.name(), n);
        }
        return out;
    }

    private static List<String> progressTrends(List<Session> completed, int total, long noShows, long cancelled,
                                               OptionalDouble avgSatisfaction) {
        List<String> out = new ArrayList<>();
        if (total == 0) {
            out.add("No sessions recorded in the last " + WINDOW_DAYS + " days.");
            return out;
        }
        out.add(String.format("%d of %d sessions completed; %d no-shows, %d cancelled.",
                completed.size(), total, noShows, cancelled));
        // Mood change: first vs latest rating per client, for clients with 2+ rated sessions.
        Map<String, List<Integer>> moods = new LinkedHashMap<>();
        for (Session s : completed) {
            String key = clientKey(s);
            if (key != null && s.getClientMoodRating() != null) moods.computeIfAbsent(key, k -> new ArrayList<>()).add(s.getClientMoodRating());
        }
        List<Integer> deltas = moods.values().stream().filter(l -> l.size() >= 2)
                .map(l -> l.get(l.size() - 1) - l.get(0)).toList();
        if (!deltas.isEmpty()) {
            long improved = deltas.stream().filter(d -> d > 0).count();
            double avg = deltas.stream().mapToInt(Integer::intValue).average().orElse(0);
            out.add(String.format("Mood rating improved for %d of %d clients seen more than once (average change %+.1f on a 1–10 scale).",
                    improved, deltas.size(), avg));
        }
        avgSatisfaction.ifPresent(a -> out.add(String.format("Average client satisfaction: %.1f / 5.", a)));
        return out;
    }

    private static String scheduleSummary(List<Session> completed, double hoursPerWeek) {
        if (completed.isEmpty()) return "Not enough completed sessions to suggest a schedule yet.";
        Map<DayOfWeek, Long> byDay = completed.stream().collect(Collectors.groupingBy(s -> s.getSessionDate().getDayOfWeek(), Collectors.counting()));
        Map<Integer, Long> byHour = completed.stream().collect(Collectors.groupingBy(s -> s.getSessionDate().getHour(), Collectors.counting()));
        String days = byDay.entrySet().stream().sorted(Map.Entry.<DayOfWeek, Long>comparingByValue().reversed()).limit(2)
                .map(e -> e.getKey().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)).collect(Collectors.joining(" and "));
        int hour = byHour.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(9);
        return String.format("Busiest days: %s; most sessions start around %02d:00. Averaging %.1f contact hours/week "
                + "over the last %d days. Keep buffer time after back-to-back slots on those days.",
                days, hour, hoursPerWeek, WINDOW_DAYS);
    }

    private static Map<String, List<String>> interventionsByOutcome(List<Session> completed) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (Session s : completed) {
            if (s.getOutcome() == null || s.getInterventions() == null || s.getInterventions().isBlank()) continue;
            List<String> list = out.computeIfAbsent(s.getOutcome().name(), k -> new ArrayList<>());
            // Intervention labels only (truncated) — not session notes or presenting issues.
            if (list.size() < 15) list.add(truncate(s.getInterventions().trim(), 120));
        }
        return out;
    }

    private static String clientKey(Session s) {
        if (s.getClient() != null && s.getClient().getId() != null) return "c" + s.getClient().getId();
        if (s.getStudent() != null && s.getStudent().getId() != null) return "u" + s.getStudent().getId();
        return null;
    }

    // =========================================================================== resource recommendations

    /**
     * Ranks resources from the real catalogue for a client. The LLM may only pick catalogue ids;
     * anything it returns that isn't in the catalogue is dropped, so it can never invent a resource.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> recommendResources(Long clientId, String context) {
        List<Resource> catalogue = resourceRepository.findAll();
        Map<String, Object> out = new LinkedHashMap<>();
        if (catalogue.isEmpty()) {
            out.put("recommendations", List.of());
            out.put("aiAvailable", false);
            out.put("aiMessage", "The resource library is empty. Add resources under Resources first.");
            return out;
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        if (clientId != null) {
            List<MentalHealthAcademicAnalysis> analyses = analysisRepository.findByClientIdOrderByCreatedAtDesc(clientId);
            if (!analyses.isEmpty()) {
                MentalHealthAcademicAnalysis a = analyses.get(0);
                profile.put("mentalHealthStatus", a.getMentalHealthStatus());
                profile.put("impactLevel", a.getImpactLevel());
                profile.put("interventionUrgency", a.getInterventionUrgency());
                List<String> factors = new ArrayList<>();
                if (Boolean.TRUE.equals(a.getConcentrationIssues())) factors.add("concentration issues");
                if (Boolean.TRUE.equals(a.getMotivationIssues())) factors.add("motivation issues");
                if (Boolean.TRUE.equals(a.getSleepIssues())) factors.add("sleep issues");
                if (Boolean.TRUE.equals(a.getSocialIsolation())) factors.add("social isolation");
                if (Boolean.TRUE.equals(a.getFinancialStress())) factors.add("financial stress");
                if (Boolean.TRUE.equals(a.getFamilyIssues())) factors.add("family issues");
                if (Boolean.TRUE.equals(a.getSubstanceUseConcern())) factors.add("substance use concern");
                profile.put("riskFactors", factors);
            }
        }
        if (context != null && !context.isBlank()) profile.put("counselorFocus", truncate(context.trim(), 500));
        boolean hasProfile = !profile.isEmpty();

        // Cap what we send: featured first, then most downloaded.
        List<Resource> shortlist = catalogue.stream()
                .sorted(Comparator.comparing(Resource::isFeatured).reversed()
                        .thenComparing(Comparator.comparingInt(Resource::getDownloadCount).reversed()))
                .limit(80).toList();
        List<Map<String, Object>> items = shortlist.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("title", r.getTitle());
            m.put("type", r.getType());
            m.put("category", r.getCategory());
            m.put("description", truncate(Objects.toString(r.getDescription(), ""), 200));
            return m;
        }).toList();

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("clientProfile", hasProfile ? profile : "No assessment on file — recommend broadly useful student wellbeing resources.");
        input.put("catalogue", items);

        try {
            AiResult r = gateway.chat("""
                    You help UNZA counselors choose self-help resources for a student. Pick up to 6 resources \
                    from the catalogue that best fit the client profile. You may ONLY use ids from the catalogue.
                    Respond with ONLY a JSON object (no markdown):
                    {"recommendations": [{"id": number, "relevance": number between 0 and 1, "reason": string under 25 words}]}
                    Order by relevance, highest first. If the profile shows crisis or high urgency, include any \
                    emergency or professional-help resources first.
                    """, List.of(AiMessage.user(toJson(input))), 900);

            Map<Long, Resource> byId = shortlist.stream().collect(Collectors.toMap(Resource::getId, Function.identity()));
            List<Map<String, Object>> recs = new ArrayList<>();
            JsonNode json = extractJson(r.content());
            if (json != null) {
                for (JsonNode n : json.path("recommendations")) {
                    Resource res = byId.get(n.path("id").asLong(-1));
                    if (res == null || recs.stream().anyMatch(x -> x.get("id").equals(String.valueOf(res.getId())))) continue;
                    double relevance = Math.max(0, Math.min(1, n.path("relevance").asDouble(0.5)));
                    recs.add(toRecommendation(res, relevance, n.path("reason").asText(""), hasProfile));
                }
            }
            out.put("recommendations", recs);
            out.put("aiAvailable", true);
            out.put("aiMessage", recs.isEmpty() ? "The AI didn't find a good match in the current library." : null);
            out.put("aiProvider", r.providerName() + " · " + r.model());
        } catch (AiUnavailableException e) {
            out.put("recommendations", List.of());
            out.put("aiAvailable", false);
            out.put("aiMessage", e.getMessage());
        }
        out.put("profileUsed", hasProfile ? profile : null);
        return out;
    }

    private static Map<String, Object> toRecommendation(Resource r, double relevance, String reason, boolean personalised) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", String.valueOf(r.getId()));
        m.put("title", r.getTitle());
        m.put("description", r.getDescription());
        m.put("type", r.getType());
        m.put("category", r.getCategory());
        m.put("relevance", Math.round(relevance * 100) / 100.0);
        m.put("reason", reason);
        m.put("link", r.getUrl() != null && !r.getUrl().isBlank() ? r.getUrl() : r.getFileUrl());
        m.put("fileName", r.getFileName());
        m.put("featured", r.isFeatured());
        m.put("clientSpecific", personalised);
        return m;
    }

    // =========================================================================== helpers

    private static <T> Map<String, Long> countBy(List<T> items, Function<T, String> key) {
        return items.stream().collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.counting()));
    }

    private String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private JsonNode extractJson(String text) {
        if (text == null) return null;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        try {
            return mapper.readTree(text.substring(start, end + 1));
        } catch (Exception e) {
            log.debug("AI response was not valid JSON: {}", e.getMessage());
            return null;
        }
    }

    private static List<String> strings(JsonNode arr) {
        List<String> out = new ArrayList<>();
        arr.forEach(n -> { if (!n.asText("").isBlank()) out.add(n.asText().trim()); });
        return out;
    }

    private static double round1(double d) {
        return Math.round(d * 10) / 10.0;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
