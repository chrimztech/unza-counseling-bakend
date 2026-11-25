package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.*;
import zm.unza.counseling.repository.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistics behind the counsellor "Sentiment Analysis" and "Predictive Analytics" screens,
 * computed only from real records (replacing the generated demo data those screens used).
 * Nothing here is LLM output, and every figure can be traced to self-assessments,
 * appointments or counsellor-entered wellness reviews.
 */
@Service
@RequiredArgsConstructor
public class AiAnalyticsService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM");
    private static final int HISTORY_WEEKS = 12;

    private final SelfAssessmentRepository selfAssessmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final MentalHealthAcademicAnalysisRepository analysisRepository;

    // ================================================================= sentiment / wellbeing trends

    /**
     * Weekly averages of PHQ-9 / GAD-7 / PSS, the latest-result distribution and per-person
     * trends. {@code counselorUserId} (optional) limits to people who have appointments with
     * that counsellor. The wellbeing index is 100 − PHQ-9 as a percentage of its maximum (27).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> sentimentTrends(Long counselorUserId, LocalDate start, LocalDate end) {
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay().minusNanos(1);
        List<SelfAssessment> all = selfAssessmentRepository.findInRange(from, to);

        if (counselorUserId != null) {
            Set<Long> people = userRepository.findById(counselorUserId)
                    .map(c -> appointmentRepository.findByCounselor(c).stream()
                            .map(Appointment::getStudent).filter(Objects::nonNull).map(User::getId)
                            .collect(Collectors.toSet()))
                    .orElse(Set.of());
            all = all.stream().filter(a -> a.getSubmittedByUserId() != null && people.contains(a.getSubmittedByUserId())).toList();
        }

        // ---- weekly buckets (Monday-start), including empty weeks so the chart has a continuous axis
        Map<LocalDate, List<SelfAssessment>> byWeek = new TreeMap<>();
        for (LocalDate w = weekStart(start); !w.isAfter(end); w = w.plusWeeks(1)) byWeek.put(w, new ArrayList<>());
        for (SelfAssessment a : all) byWeek.computeIfAbsent(weekStart(when(a).toLocalDate()), k -> new ArrayList<>()).add(a);

        List<Map<String, Object>> trends = new ArrayList<>();
        byWeek.forEach((week, items) -> {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("date", week.toString());
            t.put("averageMood", round1(avg(items.stream().map(SelfAssessment::getPhq9Score).filter(Objects::nonNull)
                    .map(s -> wellbeing(s)).toList())));
            t.put("phq9Average", round1(avg(items.stream().map(SelfAssessment::getPhq9Score).filter(Objects::nonNull).map(Integer::doubleValue).toList())));
            t.put("gad7Average", round1(avg(items.stream().map(SelfAssessment::getGad7Score).filter(Objects::nonNull).map(Integer::doubleValue).toList())));
            t.put("pssAverage", round1(avg(items.stream().map(SelfAssessment::getPssScore).filter(Objects::nonNull).map(Integer::doubleValue).toList())));
            t.put("totalAssessments", items.size());
            trends.add(t);
        });

        // ---- latest result per identifiable person (anonymous submissions count in trends only)
        Map<Long, List<SelfAssessment>> byPerson = all.stream()
                .filter(a -> a.getSubmittedByUserId() != null && !Boolean.TRUE.equals(a.getSubmittedAsAnonymous()))
                .collect(Collectors.groupingBy(SelfAssessment::getSubmittedByUserId, LinkedHashMap::new, Collectors.toList()));

        Map<String, Long> bands = new LinkedHashMap<>();
        for (String b : List.of("Minimal symptoms", "Mild", "Moderate low mood", "Severe low mood", "High anxiety", "High stress")) bands.put(b, 0L);
        for (List<SelfAssessment> list : byPerson.values()) bands.merge(band(list.get(list.size() - 1)), 1L, Long::sum);
        long people = byPerson.size();
        List<Map<String, Object>> distribution = new ArrayList<>();
        bands.forEach((label, count) -> {
            if (count == 0) return;
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("emotion", label);
            d.put("count", count);
            d.put("percentage", people == 0 ? 0 : Math.round(count * 100.0 / people));
            distribution.add(d);
        });

        // ---- per-person trend (needs two or more PHQ-9 results in the range)
        Map<Long, User> users = userRepository.findAllById(byPerson.keySet()).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> clients = new ArrayList<>();
        byPerson.forEach((uid, list) -> {
            List<Integer> phq = list.stream().map(SelfAssessment::getPhq9Score).filter(Objects::nonNull).toList();
            if (phq.size() < 2) return;
            int first = (int) Math.round(wellbeing(phq.get(0)));
            int last = (int) Math.round(wellbeing(phq.get(phq.size() - 1)));
            int change = last - first;
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("clientId", String.valueOf(uid));
            c.put("clientName", displayName(users.get(uid)));
            c.put("trend", change >= 5 ? "improving" : change <= -5 ? "declining" : "stable");
            c.put("lastScore", last);
            c.put("change", change);
            clients.add(c);
        });
        clients.sort(Comparator.comparingInt(c -> (Integer) c.get("change")));   // most declined first

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("trends", trends);
        out.put("emotionDistribution", distribution);
        out.put("clientInsights", clients);
        out.put("totalAssessments", all.size());
        out.put("peopleAssessed", people);
        out.put("method", "Weekly averages of submitted self-assessments. Wellbeing index = 100 − PHQ-9/27×100. "
                + "Distribution uses each person's latest result (PHQ-9 ≥ 20 severe, ≥ 10 moderate, ≥ 5 mild; "
                + "GAD-7 ≥ 15 high anxiety; PSS ≥ 27 high stress). Trend = change in wellbeing index (±5).");
        return out;
    }

    // ================================================================= predictive analytics

    @Transactional(readOnly = true)
    public Map<String, Object> predictive(int weeksAhead) {
        weeksAhead = Math.max(1, Math.min(12, weeksAhead));
        LocalDate thisWeek = weekStart(LocalDate.now());
        LocalDate historyStart = thisWeek.minusWeeks(HISTORY_WEEKS);
        List<Appointment> history = appointmentRepository.findActiveInRange(historyStart.atStartOfDay(), thisWeek.atStartOfDay().minusNanos(1));
        List<Appointment> future = appointmentRepository.findActiveInRange(thisWeek.atStartOfDay(), thisWeek.plusWeeks(weeksAhead).atStartOfDay());

        // ---- weekly history, linear trend, type mix
        double[] counts = new double[HISTORY_WEEKS];
        for (Appointment a : history) {
            int idx = (int) java.time.temporal.ChronoUnit.WEEKS.between(historyStart, weekStart(a.getAppointmentDate().toLocalDate()));
            if (idx >= 0 && idx < HISTORY_WEEKS) counts[idx]++;
        }
        int weeksWithData = (int) Arrays.stream(counts).filter(c -> c > 0).count();
        double n = HISTORY_WEEKS, sx = 0, sy = 0, sxy = 0, sxx = 0;
        for (int i = 0; i < HISTORY_WEEKS; i++) { sx += i; sy += counts[i]; sxy += i * counts[i]; sxx += (double) i * i; }
        double slope = (n * sxy - sx * sy) / (n * sxx - sx * sx);
        double intercept = (sy - slope * sx) / n;
        double mean = sy / n;
        double sd = Math.sqrt(Arrays.stream(counts).map(c -> (c - mean) * (c - mean)).sum() / n);
        double cv = mean > 0 ? sd / mean : 1;

        Map<Appointment.AppointmentType, Long> mix = history.stream().filter(a -> a.getType() != null)
                .collect(Collectors.groupingBy(Appointment::getType, Collectors.counting()));
        double typed = Math.max(1, mix.values().stream().mapToLong(Long::longValue).sum());
        double initialShare = mix.getOrDefault(Appointment.AppointmentType.INITIAL_CONSULTATION, 0L) / typed;
        double crisisShare = mix.getOrDefault(Appointment.AppointmentType.CRISIS_INTERVENTION, 0L) / typed;

        Map<LocalDate, Long> scheduledByWeek = future.stream()
                .collect(Collectors.groupingBy(a -> weekStart(a.getAppointmentDate().toLocalDate()), Collectors.counting()));

        List<Map<String, Object>> forecasts = new ArrayList<>();
        for (int k = 0; k < weeksAhead; k++) {
            LocalDate week = thisWeek.plusWeeks(k);
            long scheduled = scheduledByWeek.getOrDefault(week, 0L);
            double trend = Math.max(0, intercept + slope * (HISTORY_WEEKS + k));
            long predicted = Math.max(scheduled, Math.round(trend));
            // Confidence falls with noisier history, thinner data and longer horizon.
            int confidence = weeksWithData < 4 ? 0 : (int) Math.max(10, Math.min(95,
                    Math.round(90 - Math.min(60, cv * 60) - k * 4 - (HISTORY_WEEKS - weeksWithData) * 2)));
            long crisis = Math.round(predicted * crisisShare);
            long initial = Math.round(predicted * initialShare);
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("week", "Week " + (k + 1) + " (" + DAY.format(week) + ")");
            f.put("predictedAppointments", predicted);
            f.put("scheduled", scheduled);
            f.put("confidence", confidence);
            f.put("initial", initial);
            f.put("crisis", crisis);
            f.put("followUp", Math.max(0, predicted - initial - crisis));
            forecasts.add(f);
        }

        // ---- dropout risk from real attendance in the history window
        Map<Long, List<Appointment>> byStudent = history.stream().filter(a -> a.getStudent() != null)
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        Set<Long> hasUpcoming = future.stream().filter(a -> a.getStudent() != null).map(a -> a.getStudent().getId()).collect(Collectors.toSet());
        List<Map<String, Object>> atRisk = new ArrayList<>();
        byStudent.forEach((sid, list) -> {
            long noShows = list.stream().filter(a -> a.getStatus() == Appointment.AppointmentStatus.NO_SHOW
                    || a.getStatus() == Appointment.AppointmentStatus.MISSED).count();
            long cancelled = list.stream().filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED).count();
            Optional<LocalDateTime> lastAttended = list.stream().filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .map(Appointment::getAppointmentDate).max(Comparator.naturalOrder());
            boolean noFollowUp = !hasUpcoming.contains(sid);
            long daysSince = lastAttended.map(d -> java.time.Duration.between(d, LocalDateTime.now()).toDays()).orElse(-1L);

            int score = (int) Math.min(100, noShows * 25 + cancelled * 15 + (noFollowUp && daysSince > 21 ? 25 : 0)
                    + (noFollowUp && lastAttended.isEmpty() ? 15 : 0));
            if (score < 40) return;
            List<String> indicators = new ArrayList<>();
            List<String> actions = new ArrayList<>();
            if (noShows > 0) { indicators.add("Missed " + noShows + " session" + (noShows > 1 ? "s" : "") + " in " + HISTORY_WEEKS + " weeks"); actions.add("Reach out to check in"); }
            if (cancelled > 0) { indicators.add("Cancelled " + cancelled + " session" + (cancelled > 1 ? "s" : "")); actions.add("Ask about barriers (timing, transport, cost)"); }
            if (noFollowUp && daysSince > 21) { indicators.add("No follow-up booked; last seen " + daysSince + " days ago"); actions.add("Offer a follow-up appointment"); }
            if (noFollowUp && lastAttended.isEmpty()) indicators.add("Has not attended any session yet");
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("clientId", String.valueOf(sid));
            r.put("clientName", displayName(list.get(0).getStudent()));
            r.put("riskScore", score);
            r.put("indicators", indicators);
            r.put("recommendedActions", actions);
            atRisk.add(r);
        });
        atRisk.sort(Comparator.comparingInt(r -> -(Integer) r.get("riskScore")));

        // ---- high risk: latest counsellor wellness review per client
        Map<Long, MentalHealthAcademicAnalysis> latest = new LinkedHashMap<>();
        for (MentalHealthAcademicAnalysis a : analysisRepository.findHighRiskAnalyses()) {
            if (a.getClient() == null) continue;
            latest.merge(a.getClient().getId(), a, (x, y) ->
                    Optional.ofNullable(x.getCreatedAt()).orElse(LocalDateTime.MIN).isAfter(Optional.ofNullable(y.getCreatedAt()).orElse(LocalDateTime.MIN)) ? x : y);
        }
        List<Map<String, Object>> highRisk = new ArrayList<>();
        for (MentalHealthAcademicAnalysis a : latest.values()) {
            List<String> factors = new ArrayList<>();
            if (Boolean.TRUE.equals(a.getSocialIsolation())) factors.add("Social isolation");
            if (Boolean.TRUE.equals(a.getFinancialStress())) factors.add("Financial stress");
            if (Boolean.TRUE.equals(a.getFamilyIssues())) factors.add("Family issues");
            if (Boolean.TRUE.equals(a.getSubstanceUseConcern())) factors.add("Substance use");
            if (Boolean.TRUE.equals(a.getSleepIssues())) factors.add("Sleep problems");
            if (Boolean.TRUE.equals(a.getConcentrationIssues())) factors.add("Concentration");
            if (Boolean.TRUE.equals(a.getMotivationIssues())) factors.add("Motivation");
            String impact = a.getImpactLevel() == null ? "" : a.getImpactLevel().name();
            double severity = switch (impact) { case "CRITICAL" -> 0.9; case "SEVERE" -> 0.75; case "MODERATE" -> 0.55; default -> 0.4; };
            if (a.getMentalHealthStatus() == MentalHealthAcademicAnalysis.MentalHealthStatus.CRISIS) severity = Math.max(severity, 0.9);
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("id", String.valueOf(a.getClient().getId()));
            h.put("name", displayName(a.getClient()));
            h.put("probability", severity);
            h.put("factors", factors);
            h.put("status", a.getMentalHealthStatus() == MentalHealthAcademicAnalysis.MentalHealthStatus.CRISIS ? "Critical" : "High");
            h.put("reviewedAt", a.getCreatedAt());
            highRisk.add(h);
        }
        highRisk.sort(Comparator.comparingDouble(h -> -(Double) h.get("probability")));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("weeklyForecasts", forecasts);
        out.put("dropoutRisk", atRisk.size() > 15 ? atRisk.subList(0, 15) : atRisk);
        out.put("highRiskClients", highRisk.size() > 15 ? highRisk.subList(0, 15) : highRisk);
        out.put("historyWeeks", HISTORY_WEEKS);
        out.put("weeksWithData", weeksWithData);
        out.put("historyAverage", round1(mean));
        out.put("method", "Forecast: linear trend over the last " + HISTORY_WEEKS + " weeks of appointments, never below "
                + "what is already scheduled; split by the historical mix of appointment types; confidence drops with "
                + "noisy or sparse history and further weeks. Dropout risk: no-shows (25), cancellations (15), no "
                + "follow-up for 3+ weeks (25). High risk: latest counsellor wellness review marked at-risk or crisis; "
                + "severity comes from its impact level.");
        return out;
    }

    // ================================================================= helpers

    private static LocalDate weekStart(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static LocalDateTime when(SelfAssessment a) {
        return a.getAssessmentDate() != null ? a.getAssessmentDate() : a.getCreatedAt();
    }

    private static double wellbeing(int phq9) {
        return 100.0 - Math.min(27, Math.max(0, phq9)) * 100.0 / 27.0;
    }

    private static String band(SelfAssessment a) {
        Integer phq = a.getPhq9Score(), gad = a.getGad7Score(), pss = a.getPssScore();
        if (phq != null && phq >= 20) return "Severe low mood";
        if (phq != null && phq >= 10) return "Moderate low mood";
        if (gad != null && gad >= 15) return "High anxiety";
        if (pss != null && pss >= 27) return "High stress";
        if (phq != null && phq >= 5) return "Mild";
        return "Minimal symptoms";
    }

    private static double avg(List<Double> values) {
        return values.isEmpty() ? 0 : values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private static double round1(double d) {
        return Math.round(d * 10) / 10.0;
    }

    /** First name and surname initial — enough for a counsellor to recognise, minimal for a dashboard. */
    private static String displayName(User u) {
        if (u == null) return "Unknown";
        String first = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String last = u.getLastName() == null ? "" : u.getLastName().trim();
        String name = (first + (last.isEmpty() ? "" : " " + last.charAt(0) + ".")).trim();
        return name.isEmpty() ? "Student #" + u.getId() : name;
    }
}
