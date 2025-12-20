package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:UNZA Counseling Management System}")
    private String appName;

    /**
     * Send a simple text email asynchronously
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (MailException e) {
            log.error("Failed to send simple email to: {}", to, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send a rich HTML email asynchronously
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send appointment confirmation email
     */
    public CompletableFuture<Void> sendAppointmentConfirmation(String clientEmail, String clientName, 
                                                              String counselorName, String appointmentDate, 
                                                              String appointmentTime) {
        String subject = String.format("%s - Appointment Confirmation", appName);
        String htmlContent = buildAppointmentConfirmationHtml(clientName, counselorName, appointmentDate, appointmentTime);
        return sendHtmlEmail(clientEmail, subject, htmlContent);
    }

    /**
     * Send appointment reminder email
     */
    public CompletableFuture<Void> sendAppointmentReminder(String clientEmail, String clientName, 
                                                         String counselorName, String appointmentDate, 
                                                         String appointmentTime) {
        String subject = String.format("%s - Appointment Reminder", appName);
        String htmlContent = buildAppointmentReminderHtml(clientName, counselorName, appointmentDate, appointmentTime);
        return sendHtmlEmail(clientEmail, subject, htmlContent);
    }

    /**
     * Send risk assessment alert email
     */
    public CompletableFuture<Void> sendRiskAssessmentAlert(String counselorEmail, String clientName, 
                                                         String riskLevel, String assessmentDate) {
        String subject = String.format("%s - Risk Assessment Alert: %s Risk Level", appName, riskLevel);
        String htmlContent = buildRiskAssessmentAlertHtml(counselorEmail, clientName, riskLevel, assessmentDate);
        return sendHtmlEmail(counselorEmail, subject, htmlContent);
    }

    /**
     * Send welcome email to new users
     */
    public CompletableFuture<Void> sendWelcomeEmail(String userEmail, String userName, String role) {
        String subject = String.format("Welcome to %s", appName);
        String htmlContent = buildWelcomeHtml(userName, role);
        return sendHtmlEmail(userEmail, subject, htmlContent);
    }

    private String buildAppointmentConfirmationHtml(String clientName, String counselorName, 
                                                  String appointmentDate, String appointmentTime) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Appointment Confirmation</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Appointment Confirmed</h1>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Your counseling appointment has been confirmed with the following details:</p>
                        <ul>
                            <li><strong>Counselor:</strong> %s</li>
                            <li><strong>Date:</strong> %s</li>
                            <li><strong>Time:</strong> %s</li>
                        </ul>
                        <p>Please arrive 10 minutes early for your appointment. If you need to reschedule, please contact us at least 24 hours in advance.</p>
                    </div>
                    <div class="footer">
                        <p>%s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName, counselorName, appointmentDate, appointmentTime, appName);
    }

    private String buildAppointmentReminderHtml(String clientName, String counselorName, 
                                              String appointmentDate, String appointmentTime) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Appointment Reminder</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Appointment Reminder</h1>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>This is a friendly reminder about your upcoming counseling appointment:</p>
                        <ul>
                            <li><strong>Counselor:</strong> %s</li>
                            <li><strong>Date:</strong> %s</li>
                            <li><strong>Time:</strong> %s</li>
                        </ul>
                        <p>We're looking forward to seeing you!</p>
                    </div>
                    <div class="footer">
                        <p>%s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName, counselorName, appointmentDate, appointmentTime, appName);
    }

    private String buildRiskAssessmentAlertHtml(String counselorEmail, String clientName, 
                                              String riskLevel, String assessmentDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Risk Assessment Alert</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                    .alert { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Risk Assessment Alert</h1>
                    </div>
                    <div class="content">
                        <h2>Attention Required</h2>
                        <div class="alert">
                            <p><strong>Client:</strong> %s</p>
                            <p><strong>Risk Level:</strong> %s</p>
                            <p><strong>Assessment Date:</strong> %s</p>
                        </div>
                        <p>A new risk assessment has been completed that requires your attention. Please review the assessment details and take appropriate action as needed.</p>
                    </div>
                    <div class="footer">
                        <p>%s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName, riskLevel, assessmentDate, appName);
    }

    private String buildWelcomeHtml(String userName, String role) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to %s</h1>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Welcome to the %s! Your account has been successfully created with the following role:</p>
                        <p><strong>Role:</strong> %s</p>
                        <p>You can now log in to access the system and start using our counseling management services.</p>
                    </div>
                    <div class="footer">
                        <p>%s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, userName, appName, role, appName);
    }
}