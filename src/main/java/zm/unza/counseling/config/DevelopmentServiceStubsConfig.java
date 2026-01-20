package zm.unza.counseling.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import zm.unza.counseling.service.RiskAssessmentService;
import zm.unza.counseling.service.NotificationService;

import java.lang.reflect.Proxy;

@Configuration
@Profile("development")
public class DevelopmentServiceStubsConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentServiceStubsConfig.class);

    @Bean
    @ConditionalOnMissingBean(RiskAssessmentService.class)
    public RiskAssessmentService riskAssessmentService() {
        logger.warn("Creating stub for RiskAssessmentService to allow application startup. Real implementation appears to be missing.");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(RiskAssessmentService.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            logger.debug("Invoked stubbed method on RiskAssessmentService: {}", method.getName());
            if (method.getReturnType().equals(boolean.class)) return false;
            if (method.getReturnType().equals(int.class)) return 0;
            if (method.getReturnType().equals(long.class)) return 0L;
            return null;
        });
        return (RiskAssessmentService) enhancer.create();
    }

    @Bean
    @ConditionalOnMissingBean(NotificationService.class)
    public NotificationService notificationService() {
        logger.warn("Creating stub for NotificationService to allow application startup. Real implementation appears to be missing.");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(NotificationService.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            logger.debug("Invoked stubbed method on NotificationService: {}", method.getName());
            return null;
        });
        return (NotificationService) enhancer.create();
    }

    @Bean
    @ConditionalOnMissingBean(SimpMessagingTemplate.class)
    public SimpMessagingTemplate simpMessagingTemplate() {
        logger.warn("Creating stub for SimpMessagingTemplate to allow application startup. Real implementation appears to be missing.");

        // Create a dummy MessageChannel required for the SimpMessagingTemplate constructor
        MessageChannel channelStub = (MessageChannel) Proxy.newProxyInstance(
                MessageChannel.class.getClassLoader(),
                new Class[]{MessageChannel.class},
                (proxy, method, args) -> true
        );

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SimpMessagingTemplate.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            logger.debug("Invoked stubbed method on SimpMessagingTemplate: {}", method.getName());
            return null;
        });
        return (SimpMessagingTemplate) enhancer.create(new Class[]{MessageChannel.class}, new Object[]{channelStub});
    }
}