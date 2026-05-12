package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.AppointmentReminderService;
import zm.unza.counseling.service.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderServiceImpl implements AppointmentReminderService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final EmailServiceImpl emailService;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' HH:mm");

    @Override
    @Transactional
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        // Window: appointments starting between 55 and 65 minutes from now
        LocalDateTime windowStart = now.plusMinutes(55);
        LocalDateTime windowEnd = now.plusMinutes(65);

        List<Appointment> upcoming = appointmentRepository.findAppointmentsNeedingReminder(windowStart, windowEnd);
        if (upcoming.isEmpty()) {
            log.debug("No appointments needing reminders in the next hour");
            return;
        }

        log.info("Sending reminders for {} upcoming appointment(s)", upcoming.size());

        for (Appointment appt : upcoming) {
            try {
                String dateStr = appt.getAppointmentDate().format(DATE_FMT);
                String counselorName = getCounselorName(appt);
                String clientName = getClientName(appt);

                // In-app notification to client / student
                Long recipientId = appt.getClient() != null ? appt.getClient().getId()
                        : appt.getStudent() != null ? appt.getStudent().getId() : null;
                if (recipientId != null) {
                    notificationService.sendNotification(
                            recipientId,
                            "Appointment Reminder",
                            String.format("Your appointment with %s is in 1 hour — %s", counselorName, dateStr),
                            "APPOINTMENT",
                            "HIGH",
                            "/appointments/" + appt.getId()
                    );
                }

                // In-app notification to counselor
                if (appt.getCounselor() != null) {
                    notificationService.sendNotification(
                            appt.getCounselor().getId(),
                            "Upcoming Appointment",
                            String.format("Appointment with %s in 1 hour — %s", clientName, dateStr),
                            "APPOINTMENT",
                            "MEDIUM",
                            "/appointments/" + appt.getId()
                    );
                }

                // Email to client / student
                String clientEmail = appt.getClient() != null ? appt.getClient().getEmail()
                        : appt.getStudent() != null ? appt.getStudent().getEmail() : null;
                if (clientEmail != null) {
                    String time = appt.getAppointmentDate().format(DateTimeFormatter.ofPattern("HH:mm"));
                    String date = appt.getAppointmentDate().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
                    emailService.sendAppointmentReminder(clientEmail, clientName, counselorName, date, time);
                }

                // Mark reminder sent
                appt.setReminderSent(true);
                appointmentRepository.save(appt);

                log.info("Reminder sent for appointment {} at {}", appt.getId(), dateStr);
            } catch (Exception e) {
                log.error("Failed to send reminder for appointment {}", appt.getId(), e);
            }
        }
    }

    @Override
    public void sendDailySummary() {
        List<User> counselors = userRepository.findActiveByRoleName(Role.ERole.ROLE_COUNSELOR);
        if (counselors.isEmpty()) {
            log.debug("No active counselors found for daily summary");
            return;
        }

        log.info("Sending daily appointment summary to {} counselor(s)", counselors.size());

        for (User counselor : counselors) {
            try {
                List<Appointment> todayAppts = appointmentRepository.findTodayAppointmentsByCounselor(counselor);
                if (todayAppts.isEmpty()) {
                    continue;
                }

                // Build summary body
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Good morning, %s!%n%n", counselor.getFirstName()));
                sb.append(String.format("You have %d appointment(s) today:%n%n", todayAppts.size()));
                for (Appointment a : todayAppts) {
                    sb.append(String.format("  • %s — %s (%s)%n",
                            a.getAppointmentDate().format(DateTimeFormatter.ofPattern("HH:mm")),
                            getClientName(a),
                            a.getType() != null ? a.getType().toString() : "Appointment"));
                }
                sb.append("\nHave a productive day!\n— UNZA Counseling System");

                String subject = String.format("Your Appointments for Today — %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")));

                emailService.sendSimpleEmail(counselor.getEmail(), subject, sb.toString());

                // In-app notification
                notificationService.sendNotification(
                        counselor.getId(),
                        "Daily Schedule",
                        String.format("You have %d appointment(s) today. Check your schedule.", todayAppts.size()),
                        "SYSTEM",
                        "LOW",
                        "/appointments"
                );

                log.info("Daily summary sent to counselor {} with {} appointment(s)", counselor.getId(), todayAppts.size());
            } catch (Exception e) {
                log.error("Failed to send daily summary to counselor {}", counselor.getId(), e);
            }
        }
    }

    private String getCounselorName(Appointment appt) {
        if (appt.getCounselor() == null) return "your counselor";
        return appt.getCounselor().getFirstName() + " " + appt.getCounselor().getLastName();
    }

    private String getClientName(Appointment appt) {
        if (appt.getClient() != null) {
            return appt.getClient().getFirstName() + " " + appt.getClient().getLastName();
        }
        if (appt.getStudent() != null) {
            return appt.getStudent().getFirstName() + " " + appt.getStudent().getLastName();
        }
        return "client";
    }
}
