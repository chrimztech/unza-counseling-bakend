package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.response.DashboardStatsResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

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
}