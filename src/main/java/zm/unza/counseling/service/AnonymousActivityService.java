package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.response.AnonymousUserActivityDto;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.AuditLogRepository;
import zm.unza.counseling.repository.MessageRepository;
import zm.unza.counseling.repository.SelfAssessmentRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnonymousActivityService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MessageRepository messageRepository;
    private final SelfAssessmentRepository selfAssessmentRepository;
    private final AuditLogRepository auditLogRepository;

    public List<AnonymousUserActivityDto> getAnonymousActivity() {
        return userRepository.findByAnonymousTrueOrderByLastAnonymousActivityAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public Map<String, Long> getAnonymousActivityStats() {
        List<AnonymousUserActivityDto> activity = getAnonymousActivity();
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalAnonymousUsers", (long) activity.size());
        stats.put("totalAppointments", activity.stream().mapToLong(AnonymousUserActivityDto::getAppointmentCount).sum());
        stats.put("totalMessages", activity.stream().mapToLong(AnonymousUserActivityDto::getMessageCount).sum());
        stats.put("totalSelfAssessments", activity.stream().mapToLong(AnonymousUserActivityDto::getSelfAssessmentCount).sum());
        stats.put("totalAuditEvents", activity.stream().mapToLong(AnonymousUserActivityDto::getAuditEventCount).sum());
        return stats;
    }

    private AnonymousUserActivityDto toDto(User user) {
        AnonymousUserActivityDto dto = new AnonymousUserActivityDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getAnonymousDisplayName() != null && !user.getAnonymousDisplayName().isBlank()
                ? user.getAnonymousDisplayName()
                : user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLogin());
        dto.setLastActivityAt(user.getLastAnonymousActivityAt() != null ? user.getLastAnonymousActivityAt() : user.getLastLogin());
        dto.setAppointmentCount(appointmentRepository.findByStudent(user).size());
        dto.setMessageCount(messageRepository.countTotalMessages(user.getId()));
        dto.setSelfAssessmentCount(selfAssessmentRepository.findBySubmittedByUserIdOrderByAssessmentDateDesc(user.getId()).size());
        dto.setAuditEventCount(auditLogRepository.findByUserIdOrderByCreatedAtDesc(String.valueOf(user.getId())).size());
        return dto;
    }
}
