package zm.unza.counseling.service.ai;

import org.junit.jupiter.api.Test;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiAnalyticsServiceTest {

    private static SelfAssessment sa(long user, int phq, Integer gad, LocalDateTime when) {
        SelfAssessment a = new SelfAssessment();
        a.setSubmittedByUserId(user);
        a.setPhq9Score(phq);
        a.setGad7Score(gad);
        a.setAssessmentDate(when);
        return a;
    }

    @Test
    void sentimentUsesRealScoresBandsAndTrends() {
        SelfAssessmentRepository selfRepo = mock(SelfAssessmentRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        LocalDate end = LocalDate.of(2026, 11, 25);
        LocalDateTime t0 = end.minusDays(20).atTime(10, 0);
        // Person 1 improves (PHQ 18 -> 6); person 2 declines (4 -> 16); person 3 has one high-anxiety result.
        when(selfRepo.findInRange(any(), any())).thenReturn(List.of(
                sa(1, 18, 8, t0), sa(2, 4, 3, t0), sa(1, 6, 5, t0.plusDays(14)), sa(2, 16, 9, t0.plusDays(14)),
                sa(3, 3, 17, t0.plusDays(15))));
        User u1 = new User(); u1.setId(1L); u1.setFirstName("Mwila"); u1.setLastName("Banda");
        User u2 = new User(); u2.setId(2L); u2.setFirstName("Chanda");
        when(userRepo.findAllById(any())).thenReturn(List.of(u1, u2));

        AiAnalyticsService svc = new AiAnalyticsService(selfRepo, mock(AppointmentRepository.class), userRepo,
                mock(MentalHealthAcademicAnalysisRepository.class));
        Map<String, Object> out = svc.sentimentTrends(null, end.minusDays(30), end);

        assertEquals(5, out.get("totalAssessments"));
        assertEquals(3L, out.get("peopleAssessed"));
        List<Map<String, Object>> clients = (List<Map<String, Object>>) out.get("clientInsights");
        assertEquals(2, clients.size(), "only people with 2+ PHQ-9 results get a trend");
        assertEquals("declining", clients.get(0).get("trend"));   // sorted most-declined first
        assertEquals("Chanda", clients.get(0).get("clientName"));
        assertEquals("improving", clients.get(1).get("trend"));
        assertEquals("Mwila B.", clients.get(1).get("clientName"));

        List<Map<String, Object>> dist = (List<Map<String, Object>>) out.get("emotionDistribution");
        assertTrue(dist.stream().anyMatch(d -> d.get("emotion").equals("Moderate low mood") && d.get("count").equals(1L)));
        assertTrue(dist.stream().anyMatch(d -> d.get("emotion").equals("High anxiety") && d.get("count").equals(1L)));
        assertTrue(dist.stream().anyMatch(d -> d.get("emotion").equals("Mild") && d.get("count").equals(1L)));
        System.out.println("weekly trends: " + out.get("trends"));
    }

    @Test
    void forecastNeverBelowScheduledAndFlagsNoShows() {
        AppointmentRepository apptRepo = mock(AppointmentRepository.class);
        User s = new User(); s.setId(5L); s.setFirstName("Bwalya");
        List<Appointment> history = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().minusWeeks(11);
        for (int w = 0; w < 11; w++) for (int i = 0; i < 4; i++) {          // steady ~4/week
            Appointment a = new Appointment();
            a.setAppointmentDate(base.plusWeeks(w).plusHours(i));
            a.setType(Appointment.AppointmentType.FOLLOW_UP);
            a.setStatus(Appointment.AppointmentStatus.COMPLETED);
            history.add(a);
        }
        for (int i = 0; i < 2; i++) {                                         // one student: 2 no-shows, no follow-up
            Appointment a = new Appointment();
            a.setStudent(s);
            a.setAppointmentDate(LocalDateTime.now().minusWeeks(3 + i));
            a.setStatus(Appointment.AppointmentStatus.NO_SHOW);
            history.add(a);
        }
        List<Appointment> future = new ArrayList<>();
        for (int i = 0; i < 9; i++) {                                         // 9 already booked this week
            Appointment a = new Appointment();
            a.setAppointmentDate(LocalDateTime.now().plusHours(1 + i));
            future.add(a);
        }
        when(apptRepo.findActiveInRange(any(), any())).thenReturn(history, future);
        MentalHealthAcademicAnalysisRepository mhRepo = mock(MentalHealthAcademicAnalysisRepository.class);
        when(mhRepo.findHighRiskAnalyses()).thenReturn(List.of());

        AiAnalyticsService svc = new AiAnalyticsService(mock(SelfAssessmentRepository.class), apptRepo,
                mock(UserRepository.class), mhRepo);
        Map<String, Object> out = svc.predictive(4);

        List<Map<String, Object>> f = (List<Map<String, Object>>) out.get("weeklyForecasts");
        assertEquals(4, f.size());
        long week1 = (Long) f.get(0).get("predictedAppointments");
        assertTrue(week1 >= 9, "never below the 9 already scheduled, got " + week1);
        assertTrue((Integer) f.get(1).get("confidence") > 0, "11 weeks of data -> non-zero confidence");

        List<Map<String, Object>> risk = (List<Map<String, Object>>) out.get("dropoutRisk");
        assertEquals(1, risk.size());
        assertEquals("Bwalya", risk.get(0).get("clientName"));
        assertTrue((Integer) risk.get(0).get("riskScore") >= 50);
        System.out.println("forecasts: " + f + "\nrisk: " + risk);
    }
}
