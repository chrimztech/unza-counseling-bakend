package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.AppointmentDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public List<AppointmentDto> getUpcomingAppointments() {
        log.info("Fetching upcoming appointments for dashboard");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusDays(7);
        
        // Use the fetch query to eagerly load student and counselor
        List<Appointment> appointments = appointmentRepository.findByStatusAndDateRangeWithFetch(
                Appointment.AppointmentStatus.SCHEDULED,
                now,
                endOfWeek
        );
        
        // Convert to DTOs to avoid lazy-loading issues
        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private AppointmentDto convertToDto(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setTitle(appointment.getTitle());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setDuration(appointment.getDuration());
        dto.setType(appointment.getType());
        dto.setStatus(appointment.getStatus());
        dto.setSessionMode(appointment.getSessionMode());
        dto.setDescription(appointment.getDescription());
        dto.setMeetingLink(appointment.getMeetingLink());
        dto.setMeetingProvider(appointment.getMeetingProvider());
        dto.setLocation(appointment.getLocation());
        dto.setCreatedAt(appointment.getCreatedAt());
        
        // Safely access student information
        if (appointment.getStudent() != null) {
            dto.setStudentId(appointment.getStudent().getId());
            dto.setStudentName(appointment.getStudent().getFirstName() + " " + appointment.getStudent().getLastName());
        }
        
        // Safely access counselor information (may be null for unassigned appointments)
        if (appointment.getCounselor() != null) {
            dto.setCounselorId(appointment.getCounselor().getId());
            dto.setCounselorName(appointment.getCounselor().getFirstName() + " " + appointment.getCounselor().getLastName());
        } else {
            dto.setCounselorId(null);
            dto.setCounselorName("Unassigned");
        }
        
        return dto;
    }
}
