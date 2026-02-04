package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.response.DashboardStatsResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public DashboardStatsResponse getStats() {
        long totalClients = clientRepository.count();
        long activeClients = clientRepository.countByClientStatus(Client.ClientStatus.ACTIVE);
        long totalSessions = sessionRepository.count();
        
        // Count pending appointments for the next year
        long pendingAppointments = appointmentRepository.countByStatusAndDateRange(
                Appointment.AppointmentStatus.SCHEDULED,
                LocalDateTime.now(),
                LocalDateTime.now().plusYears(1)
        );
        
        long highRiskClients = clientRepository.countByRiskLevels(
                List.of(Client.RiskLevel.HIGH, Client.RiskLevel.CRITICAL)
        );
        
        long totalCounselors = userRepository.countCounselors();
        
        // Placeholder for average satisfaction calculation
        double averageSatisfaction = 0.0;

        return DashboardStatsResponse.builder()
                .totalClients(totalClients)
                .activeClients(activeClients)
                .totalSessions(totalSessions)
                .pendingAppointments(pendingAppointments)
                .highRiskClients(highRiskClients)
                .totalCounselors(totalCounselors)
                .averageSatisfaction(averageSatisfaction)
                .build();
    }

    public List<Client> getHighRiskClients() {
        return clientRepository.findByRiskLevelsOrderByRiskScoreDesc(
                List.of(Client.RiskLevel.HIGH, Client.RiskLevel.CRITICAL)
        );
    }

    public List<Client> getRecentClients() {
        return clientRepository.findTop10ByOrderByCreatedAtDesc(PageRequest.of(0, 10));
    }

    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalAppointments = appointmentRepository.count();
        List<Appointment> completedList = appointmentRepository.findByStatus(Appointment.AppointmentStatus.COMPLETED);
        List<Appointment> cancelledList = appointmentRepository.findByStatus(Appointment.AppointmentStatus.CANCELLED);
        List<Appointment> scheduledList = appointmentRepository.findByStatus(Appointment.AppointmentStatus.SCHEDULED);
        
        long completedAppointments = completedList.size();
        long cancelledAppointments = cancelledList.size();
        long scheduledAppointments = scheduledList.size();
        
        double completionRate = totalAppointments > 0 
                ? (double) completedAppointments / totalAppointments * 100 
                : 0;
        
        metrics.put("totalAppointments", totalAppointments);
        metrics.put("completedAppointments", completedAppointments);
        metrics.put("cancelledAppointments", cancelledAppointments);
        metrics.put("scheduledAppointments", scheduledAppointments);
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        
        return metrics;
    }

    public List<Appointment> getUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusDays(7);
        return appointmentRepository.findByStatusAndDateRange(
                Appointment.AppointmentStatus.SCHEDULED,
                now,
                endOfWeek
        );
    }
}
