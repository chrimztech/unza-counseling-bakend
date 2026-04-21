package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.InterventionReport;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.StudentAnalysisSummary;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.mapper.MentalHealthAcademicMapper;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.MentalHealthAcademicAnalysisRepository;
import zm.unza.counseling.repository.RiskAssessmentRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MentalHealthAcademicAnalysisRepository analysisRepository;
    private final MentalHealthAcademicMapper mapper;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AppointmentRepository appointmentRepository;

    public InterventionReport getInterventionReport() {
        List<MentalHealthAcademicAnalysis> analyses = analysisRepository.findAll();
        long totalInterventions = analysisRepository.countByInterventionNeeded(true);
        long counselingRecommended = analysisRepository.countByCounselingRecommended(true);
        long academicSupportRecommended = analysisRepository.countByAcademicSupportRecommended(true);
        long peerSupportRecommended = analysisRepository.countByPeerSupportRecommended(true);
        long lifestyleChangesRecommended = analysisRepository.countByLifestyleChangesRecommended(true);
        long referralRecommended = analysisRepository.countByReferralRecommended(true);

        List<StudentAnalysisSummary> highPriorityStudents = analysisRepository.findUrgentInterventions().stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());

        Map<MentalHealthAcademicAnalysis.InterventionUrgency, Long> urgencyDistribution = analyses.stream()
                .map(MentalHealthAcademicAnalysis::getInterventionUrgency)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        urgency -> urgency,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        InterventionReport report = InterventionReport.builder()
                .totalStudentsAnalyzed(analysisRepository.count())
                .studentsNeedingIntervention(totalInterventions)
                .counselingRecommended(counselingRecommended)
                .academicSupportRecommended(academicSupportRecommended)
                .peerSupportRecommended(peerSupportRecommended)
                .lifestyleChangesRecommended(lifestyleChangesRecommended)
                .referralRecommended(referralRecommended)
                .urgentCases(highPriorityStudents)
                .build();
        report.setUrgencyDistribution(urgencyDistribution);
        return report;
    }

    public Object getCounselorPerformanceAnalytics() {
        List<User> counselors = userRepository.findByRoleName(Role.ERole.ROLE_COUNSELOR);

        return counselors.stream()
                .filter(Objects::nonNull)
                .map(this::buildCounselorPerformanceItem)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(item -> String.valueOf(item.getOrDefault("counselorName", ""))))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildCounselorPerformanceItem(User counselor) {
        try {
            List<Session> sessions = sessionRepository.findByCounselor(counselor);
            long totalSessions = sessions.size();
            long completedSessions = sessions.stream()
                    .filter(session -> session.getStatus() == Session.SessionStatus.COMPLETED)
                    .count();
            long cancelledSessions = sessions.stream()
                    .filter(session -> session.getStatus() == Session.SessionStatus.CANCELLED)
                    .count();
            double averageSessionDuration = average(
                    sessions.stream().map(Session::getDurationMinutes).filter(Objects::nonNull).toList()
            );
            double clientSatisfaction = average(
                    sessions.stream().map(Session::getStudentSatisfactionRating).filter(Objects::nonNull).toList()
            );
            double effectiveness = totalSessions == 0
                    ? 0
                    : round((completedSessions * 100.0) / totalSessions);

            Map<String, Object> analytics = new LinkedHashMap<>();
            analytics.put("counselorId", counselor.getId());
            analytics.put("counselorName", safeCounselorName(counselor));
            analytics.put("totalSessions", totalSessions);
            analytics.put("completedSessions", completedSessions);
            analytics.put("cancelledSessions", cancelledSessions);
            analytics.put("averageSessionDuration", round(averageSessionDuration));
            analytics.put("clientSatisfaction", round(clientSatisfaction));
            analytics.put("effectiveness", round(effectiveness));
            return analytics;
        } catch (Exception exception) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("counselorId", counselor.getId());
            fallback.put("counselorName", safeCounselorName(counselor));
            fallback.put("totalSessions", 0);
            fallback.put("completedSessions", 0);
            fallback.put("cancelledSessions", 0);
            fallback.put("averageSessionDuration", 0.0);
            fallback.put("clientSatisfaction", 0.0);
            fallback.put("effectiveness", 0.0);
            fallback.put("warning", "Some counselor metrics could not be calculated");
            return fallback;
        }
    }

    private String safeCounselorName(User counselor) {
        String firstName = counselor.getFirstName() != null ? counselor.getFirstName().trim() : "";
        String lastName = counselor.getLastName() != null ? counselor.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? counselor.getEmail() : fullName;
    }

    public Object getClientDemographics() {
        List<Client> clients = clientRepository.findAll();
        int totalClients = clients.size();

        Map<String, Long> genderCounts = clients.stream()
                .map(client -> client.getGender() != null ? client.getGender().name() : "OTHER")
                .collect(Collectors.groupingBy(gender -> gender, LinkedHashMap::new, Collectors.counting()));

        List<Map<String, Object>> byFaculty = clients.stream()
                .filter(client -> client.getFaculty() != null && !client.getFaculty().isBlank())
                .collect(Collectors.groupingBy(Client::getFaculty, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("faculty", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("percentage", totalClients == 0 ? 0 : round(entry.getValue() * 100.0 / totalClients));
                    return item;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> byYearOfStudy = clients.stream()
                .filter(client -> client.getYearOfStudy() != null)
                .collect(Collectors.groupingBy(Client::getYearOfStudy, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("year", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("percentage", totalClients == 0 ? 0 : round(entry.getValue() * 100.0 / totalClients));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Long> byRiskLevel = clients.stream()
                .map(client -> client.getRiskLevel() != null ? client.getRiskLevel().name() : Client.RiskLevel.LOW.name())
                .collect(Collectors.groupingBy(level -> level, LinkedHashMap::new, Collectors.counting()));

        Map<String, Object> demographics = new LinkedHashMap<>();
        demographics.put("totalClients", totalClients);
        demographics.put("byGender", Map.of(
                "MALE", genderCounts.getOrDefault("MALE", 0L),
                "FEMALE", genderCounts.getOrDefault("FEMALE", 0L),
                "OTHER", genderCounts.getOrDefault("OTHER", 0L)
        ));
        demographics.put("byFaculty", byFaculty);
        demographics.put("byYearOfStudy", byYearOfStudy);
        demographics.put("byRiskLevel", Map.of(
                "LOW", byRiskLevel.getOrDefault("LOW", 0L),
                "MODERATE", byRiskLevel.getOrDefault("MODERATE", 0L),
                "HIGH", byRiskLevel.getOrDefault("HIGH", 0L),
                "CRITICAL", byRiskLevel.getOrDefault("CRITICAL", 0L)
        ));
        return demographics;
    }

    public Object getSessionAnalytics() {
        List<Session> sessions = sessionRepository.findAll();
        long totalSessions = sessions.size();
        long completedSessions = sessions.stream()
                .filter(session -> session.getStatus() == Session.SessionStatus.COMPLETED)
                .count();

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("totalSessions", totalSessions);
        analytics.put("sessionsByType", sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getType() != null ? session.getType().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> Map.of("type", entry.getKey(), "count", entry.getValue()))
                .collect(Collectors.toList()));
        analytics.put("sessionsByStatus", sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getStatus() != null ? session.getStatus().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> Map.of("status", entry.getKey(), "count", entry.getValue()))
                .collect(Collectors.toList()));
        analytics.put("averageSessionDuration", round(
                average(sessions.stream().map(Session::getDurationMinutes).filter(Objects::nonNull).toList())
        ));
        analytics.put("completionRate", totalSessions == 0 ? 0 : round(completedSessions * 100.0 / totalSessions));
        analytics.put("trends", buildSessionTrends(sessions));
        return analytics;
    }

    public Object getRiskAssessmentAnalytics() {
        List<RiskAssessment> assessments = riskAssessmentRepository.findAll();
        long totalAssessments = assessments.size();
        long highRiskCount = assessments.stream()
                .filter(assessment -> assessment.getRiskLevel() == Client.RiskLevel.HIGH)
                .count();
        long criticalRiskCount = assessments.stream()
                .filter(assessment -> assessment.getRiskLevel() == Client.RiskLevel.CRITICAL)
                .count();

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("totalAssessments", totalAssessments);
        analytics.put("assessmentsByRiskLevel", assessments.stream()
                .collect(Collectors.groupingBy(
                        assessment -> assessment.getRiskLevel() != null ? assessment.getRiskLevel().name() : "LOW",
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> Map.of("level", entry.getKey(), "count", entry.getValue()))
                .collect(Collectors.toList()));
        analytics.put("highRiskCount", highRiskCount);
        analytics.put("criticalRiskCount", criticalRiskCount);
        analytics.put("averageRiskScore", round(
                average(assessments.stream().map(RiskAssessment::getRiskScore).filter(Objects::nonNull).toList())
        ));
        analytics.put("trends", buildRiskTrends(assessments));
        analytics.put("riskFactors", buildRiskFactors(assessments));
        return analytics;
    }

    public Object getTimeAnalysis() {
        List<Appointment> appointments = appointmentRepository.findAll();
        List<Session> sessions = sessionRepository.findAll();

        List<Map<String, Object>> peakHours = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate() != null)
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getAppointmentDate().getHour(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("hour", entry.getKey());
                    item.put("appointments", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> peakDays = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate() != null)
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getAppointmentDate().getDayOfWeek(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("day", readableDay(entry.getKey()));
                    item.put("appointments", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        double averageWaitTime = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate() != null && appointment.getCreatedAt() != null)
                .mapToLong(appointment -> Duration.between(appointment.getCreatedAt(), appointment.getAppointmentDate()).toHours())
                .average()
                .orElse(0);

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("peakHours", peakHours);
        analysis.put("peakDays", peakDays);
        analysis.put("averageWaitTime", round(averageWaitTime));
        analysis.put("averageSessionDuration", round(
                average(sessions.stream().map(Session::getDurationMinutes).filter(Objects::nonNull).toList())
        ));
        return analysis;
    }

    public Object getOutcomesAnalytics() {
        List<Session> sessions = sessionRepository.findAll().stream()
                .filter(session -> session.getOutcome() != null)
                .collect(Collectors.toList());

        long improved = sessions.stream()
                .filter(session -> session.getOutcome() == Session.Outcome.EXCELLENT || session.getOutcome() == Session.Outcome.GOOD)
                .count();
        long stable = sessions.stream()
                .filter(session -> session.getOutcome() == Session.Outcome.FAIR || session.getOutcome() == Session.Outcome.MINIMAL)
                .count();
        long declined = sessions.stream()
                .filter(session -> session.getOutcome() == Session.Outcome.REFER)
                .count();
        long totalOutcomes = sessions.size();

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("totalOutcomes", totalOutcomes);
        analytics.put("improved", improved);
        analytics.put("stable", stable);
        analytics.put("declined", declined);
        analytics.put("improvementRate", totalOutcomes == 0 ? 0 : round(improved * 100.0 / totalOutcomes));
        analytics.put("byInterventionType", Arrays.stream(Session.SessionType.values())
                .map(type -> {
                    List<Session> typeSessions = sessions.stream()
                            .filter(session -> session.getType() == type)
                            .toList();
                    long successful = typeSessions.stream()
                            .filter(session -> session.getOutcome() == Session.Outcome.EXCELLENT || session.getOutcome() == Session.Outcome.GOOD)
                            .count();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", type.name());
                    item.put("successRate", typeSessions.isEmpty() ? 0 : round(successful * 100.0 / typeSessions.size()));
                    return item;
                })
                .collect(Collectors.toList()));
        return analytics;
    }

    public byte[] exportAnalytics(String format) {
        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("generatedAt", LocalDateTime.now());
        analytics.put("counselorPerformance", getCounselorPerformanceAnalytics());
        analytics.put("clientDemographics", getClientDemographics());
        analytics.put("sessionAnalytics", getSessionAnalytics());
        analytics.put("riskAssessmentAnalytics", getRiskAssessmentAnalytics());
        analytics.put("timeAnalysis", getTimeAnalysis());
        analytics.put("outcomesAnalytics", getOutcomesAnalytics());

        String normalizedFormat = format == null ? "csv" : format.toLowerCase(Locale.ROOT);
        if ("csv".equals(normalizedFormat) || "excel".equals(normalizedFormat)) {
            StringBuilder csv = new StringBuilder();
            csv.append("section,metric,value\n");

            Map<String, Object> clientDemographics = castMap(getClientDemographics());
            csv.append("clients,totalClients,").append(clientDemographics.get("totalClients")).append("\n");

            Map<String, Object> sessionAnalytics = castMap(getSessionAnalytics());
            csv.append("sessions,totalSessions,").append(sessionAnalytics.get("totalSessions")).append("\n");
            csv.append("sessions,averageSessionDuration,").append(sessionAnalytics.get("averageSessionDuration")).append("\n");
            csv.append("sessions,completionRate,").append(sessionAnalytics.get("completionRate")).append("\n");

            Map<String, Object> riskAnalytics = castMap(getRiskAssessmentAnalytics());
            csv.append("risk,totalAssessments,").append(riskAnalytics.get("totalAssessments")).append("\n");
            csv.append("risk,highRiskCount,").append(riskAnalytics.get("highRiskCount")).append("\n");
            csv.append("risk,criticalRiskCount,").append(riskAnalytics.get("criticalRiskCount")).append("\n");

            Map<String, Object> outcomesAnalytics = castMap(getOutcomesAnalytics());
            csv.append("outcomes,totalOutcomes,").append(outcomesAnalytics.get("totalOutcomes")).append("\n");
            csv.append("outcomes,improved,").append(outcomesAnalytics.get("improved")).append("\n");
            csv.append("outcomes,stable,").append(outcomesAnalytics.get("stable")).append("\n");
            csv.append("outcomes,declined,").append(outcomesAnalytics.get("declined")).append("\n");

            return csv.toString().getBytes(StandardCharsets.UTF_8);
        }

        return analytics.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<Map<String, Object>> buildSessionTrends(List<Session> sessions) {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> trends = new ArrayList<>();

        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = YearMonth.from(now.minusMonths(offset));
            long count = sessions.stream()
                    .filter(session -> session.getSessionDate() != null)
                    .filter(session -> YearMonth.from(session.getSessionDate()).equals(month))
                    .count();
            trends.add(Map.of("period", month.toString(), "sessions", count));
        }

        return trends;
    }

    private List<Map<String, Object>> buildRiskTrends(List<RiskAssessment> assessments) {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> trends = new ArrayList<>();

        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = YearMonth.from(now.minusMonths(offset));
            List<RiskAssessment> monthAssessments = assessments.stream()
                    .filter(assessment -> assessment.getAssessmentDate() != null)
                    .filter(assessment -> YearMonth.from(assessment.getAssessmentDate()).equals(month))
                    .toList();
            long highRisk = monthAssessments.stream()
                    .filter(assessment -> assessment.getRiskLevel() == Client.RiskLevel.HIGH || assessment.getRiskLevel() == Client.RiskLevel.CRITICAL)
                    .count();

            Map<String, Object> trend = new LinkedHashMap<>();
            trend.put("date", month.atDay(1).atStartOfDay().toString());
            trend.put("assessments", monthAssessments.size());
            trend.put("highRisk", highRisk);
            trends.add(trend);
        }

        return trends;
    }

    private List<Map<String, Object>> buildRiskFactors(List<RiskAssessment> assessments) {
        int totalAssessments = Math.max(assessments.size(), 1);
        Map<String, Long> factors = new LinkedHashMap<>();
        factors.put("Follow-up required", assessments.stream().filter(item -> Boolean.TRUE.equals(item.getFollowUpRequired())).count());
        factors.put("Critical risk", assessments.stream().filter(item -> item.getRiskLevel() == Client.RiskLevel.CRITICAL).count());
        factors.put("High risk", assessments.stream().filter(item -> item.getRiskLevel() == Client.RiskLevel.HIGH).count());
        factors.put("Moderate risk", assessments.stream().filter(item -> item.getRiskLevel() == Client.RiskLevel.MODERATE).count());
        factors.put("Low risk", assessments.stream().filter(item -> item.getRiskLevel() == Client.RiskLevel.LOW).count());

        return factors.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("factor", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("percentage", round(entry.getValue() * 100.0 / totalAssessments));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String readableDay(DayOfWeek dayOfWeek) {
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    private double average(List<? extends Number> values) {
        return values.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }
}
