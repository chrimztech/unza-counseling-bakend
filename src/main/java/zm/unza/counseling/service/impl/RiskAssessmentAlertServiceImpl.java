package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.RiskAssessmentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.NotificationService;
import zm.unza.counseling.service.RiskAssessmentAlertService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentAlertServiceImpl implements RiskAssessmentAlertService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailServiceImpl emailService;

    @Override
    public void checkHighRiskAssessments() {
        // Alert for assessments flagged as needing follow-up
        List<RiskAssessment> pending = riskAssessmentRepository.findByFollowUpRequiredTrue();
        if (pending.isEmpty()) {
            log.debug("No pending high-risk follow-up assessments");
            return;
        }

        log.info("Found {} risk assessment(s) requiring follow-up", pending.size());

        List<User> admins = userRepository.findActiveByRoleName(Role.ERole.ROLE_ADMIN);
        List<User> counselors = userRepository.findActiveByRoleName(Role.ERole.ROLE_COUNSELOR);

        for (RiskAssessment assessment : pending) {
            try {
                Client client = assessment.getClient();
                if (client == null) continue;

                String clientName = client.getFirstName() + " " + client.getLastName();
                String riskLevel = assessment.getRiskLevel() != null ? assessment.getRiskLevel().name() : "UNKNOWN";
                String assessmentDate = assessment.getAssessmentDate() != null
                        ? assessment.getAssessmentDate().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                        : "Unknown date";

                String title = "Follow-Up Required: " + riskLevel + " Risk Client";
                String message = String.format("Client %s was assessed as %s risk on %s and requires follow-up.",
                        clientName, riskLevel, assessmentDate);

                // Notify all active admins
                for (User admin : admins) {
                    notificationService.sendNotification(
                            admin.getId(), title, message, "RISK_ASSESSMENT", "HIGH", "/risk-assessments");
                }

                // Notify all active counselors
                for (User counselor : counselors) {
                    notificationService.sendNotification(
                            counselor.getId(), title, message, "RISK_ASSESSMENT", "HIGH", "/risk-assessments");
                }

                log.info("Alerts sent for follow-up on client {} ({})", client.getId(), riskLevel);
            } catch (Exception e) {
                log.error("Failed to send alert for risk assessment {}", assessment.getId(), e);
            }
        }
    }

    @Override
    public void sendDailyRiskSummary() {
        List<Client> highRiskClients = clientRepository.findByRiskLevelsOrderByRiskScoreDesc(
                List.of(Client.RiskLevel.HIGH, Client.RiskLevel.CRITICAL));

        List<User> admins = userRepository.findActiveByRoleName(Role.ERole.ROLE_ADMIN);
        if (admins.isEmpty()) {
            log.debug("No active admins found for daily risk summary");
            return;
        }

        log.info("Sending daily risk summary: {} high/critical risk client(s)", highRiskClients.size());

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy"));

        // Build summary text
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Daily Risk Summary — %s%n%n", dateStr));
        if (highRiskClients.isEmpty()) {
            sb.append("No clients are currently flagged as HIGH or CRITICAL risk.%n");
        } else {
            sb.append(String.format("%d client(s) are currently at HIGH or CRITICAL risk:%n%n", highRiskClients.size()));
            for (Client client : highRiskClients) {
                sb.append(String.format("  • %s %s — %s risk (score: %s)%n",
                        client.getFirstName(),
                        client.getLastName(),
                        client.getRiskLevel().name(),
                        client.getRiskScore() != null ? client.getRiskScore() : "N/A"));
            }
        }
        sb.append("\n— UNZA Counseling System");

        String subject = String.format("Daily Risk Summary — %s", dateStr);
        String body = sb.toString();

        for (User admin : admins) {
            try {
                emailService.sendSimpleEmail(admin.getEmail(), subject, body);
                notificationService.sendNotification(
                        admin.getId(),
                        "Daily Risk Summary",
                        String.format("%d client(s) at HIGH/CRITICAL risk as of %s", highRiskClients.size(), dateStr),
                        "SYSTEM",
                        highRiskClients.isEmpty() ? "LOW" : "HIGH",
                        "/admin/analytics"
                );
                log.info("Daily risk summary sent to admin {}", admin.getId());
            } catch (Exception e) {
                log.error("Failed to send daily risk summary to admin {}", admin.getId(), e);
            }
        }
    }
}
