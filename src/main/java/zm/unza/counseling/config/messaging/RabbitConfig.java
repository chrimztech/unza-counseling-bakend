package zm.unza.counseling.config.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    // Exchange names
    public static final String COUNSELING_EXCHANGE = "counseling.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String AUDIT_EXCHANGE = "audit.exchange";

    // Queue names
    public static final String APPOINTMENT_QUEUE = "appointment.queue";
    public static final String RISK_ALERT_QUEUE = "risk.alert.queue";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String AUDIT_LOG_QUEUE = "audit.log.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    // Routing keys
    public static final String APPOINTMENT_CREATED_KEY = "appointment.created";
    public static final String APPOINTMENT_UPDATED_KEY = "appointment.updated";
    public static final String RISK_ASSESSMENT_KEY = "risk.assessment.high";
    public static final String EMAIL_SEND_KEY = "email.send";
    public static final String AUDIT_LOG_KEY = "audit.log";
    public static final String NOTIFICATION_SEND_KEY = "notification.send";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange counselingExchange() {
        return new TopicExchange(COUNSELING_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }

    @Bean
    public Queue appointmentQueue() {
        return new Queue(APPOINTMENT_QUEUE, true);
    }

    @Bean
    public Queue riskAlertQueue() {
        return new Queue(RISK_ALERT_QUEUE, true);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public Queue auditLogQueue() {
        return new Queue(AUDIT_LOG_QUEUE, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding appointmentBinding() {
        return BindingBuilder.bind(appointmentQueue())
                .to(counselingExchange())
                .with(APPOINTMENT_CREATED_KEY);
    }

    @Bean
    public Binding appointmentUpdateBinding() {
        return BindingBuilder.bind(appointmentQueue())
                .to(counselingExchange())
                .with(APPOINTMENT_UPDATED_KEY);
    }

    @Bean
    public Binding riskAlertBinding() {
        return BindingBuilder.bind(riskAlertQueue())
                .to(counselingExchange())
                .with(RISK_ASSESSMENT_KEY);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(notificationExchange())
                .with(EMAIL_SEND_KEY);
    }

    @Bean
    public Binding auditLogBinding() {
        return BindingBuilder.bind(auditLogQueue())
                .to(auditExchange())
                .with(AUDIT_LOG_KEY);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_SEND_KEY);
    }
}