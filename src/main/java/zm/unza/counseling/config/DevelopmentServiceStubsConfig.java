package zm.unza.counseling.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import zm.unza.counseling.security.external.ExternalAuthenticationService;
import zm.unza.counseling.security.external.ExternalAuthResponse;
import zm.unza.counseling.security.external.ExternalAuthenticationException;
import zm.unza.counseling.security.AuthenticationSource;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.service.RiskAssessmentService;
import zm.unza.counseling.service.NotificationService;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.HashSet;

@Configuration
@Profile("development")
public class DevelopmentServiceStubsConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentServiceStubsConfig.class);

    @Bean("hrAuthenticationService")
    @Primary
    @Profile("development")
    public ExternalAuthenticationService hrAuthenticationServiceStub() {
        logger.warn("Creating stub for HrAuthenticationService in development mode");
        return new ExternalAuthenticationService() {
            @Override
            public ExternalAuthResponse authenticate(String username, String password)
                    throws ExternalAuthenticationException {
                logger.debug("Stub HR Authentication attempt for: {}", username);

                // Test credentials for HR staff - chrishent.mutondo@unza.zm
                if (username.equals("chrishent.mutondo@unza.zm") && password.equals("62825252Ch.")) {
                    logger.info("Stub HR authentication successful for: {}", username);
                    User stubUser = new User();
                    stubUser.setUsername("012744");
                    stubUser.setEmail("chrishent.mutondo@unza.zm");
                    stubUser.setFirstName("Christopher");
                    stubUser.setLastName("Mutondo");
                    stubUser.setDepartment("Student Services - Counselor");
                    stubUser.setPhoneNumber("+260 977 123 456");
                    stubUser.setActive(true);
                    stubUser.setEmailVerified(true);
                    stubUser.setAuthenticationSource(AuthenticationSource.HR);
                    stubUser.setCreatedAt(LocalDateTime.now());
                    stubUser.setUpdatedAt(LocalDateTime.now());
                    stubUser.setPassword("EXTERNALLY_AUTHENTICATED_USER_NO_PASSWORD");
                    stubUser.setRoles(new HashSet<>());

                    return new ExternalAuthResponse(true, "Authentication successful", stubUser, "012744", "HR");
                }

                // Original test credentials for chrishentmatakala@yahoo.com
                if (username.equals("chrishentmatakala@yahoo.com") && password.equals("62825252Ch")) {
                    logger.info("Stub HR authentication successful for: {}", username);
                    User stubUser = new User();
                    stubUser.setUsername("CHRISTOPHER.HENTMAKALA");
                    stubUser.setEmail("chrishentmatakala@yahoo.com");
                    stubUser.setFirstName("Christopher");
                    stubUser.setLastName("Hentmakala");
                    stubUser.setDepartment("Student Services - Counselor");
                    stubUser.setPhoneNumber("+260 977 123 456");
                    stubUser.setActive(true);
                    stubUser.setEmailVerified(true);
                    stubUser.setAuthenticationSource(AuthenticationSource.HR);
                    stubUser.setCreatedAt(LocalDateTime.now());
                    stubUser.setUpdatedAt(LocalDateTime.now());
                    stubUser.setPassword("EXTERNALLY_AUTHENTICATED_USER_NO_PASSWORD");
                    stubUser.setRoles(new HashSet<>());

                    return new ExternalAuthResponse(true, "Authentication successful", stubUser,
                            "CHRISTOPHER.HENTMAKALA", "HR");
                }

                logger.warn("Stub HR authentication failed for: {}", username);
                return new ExternalAuthResponse(false, "Invalid staff credentials");
            }

            @Override
            public boolean validateUserExists(String username) {
                return username.equals("chrishent.mutondo@unza.zm") || username.equals("chrishentmatakala@yahoo.com");
            }

            @Override
            public User getUserDetails(String username) throws ExternalAuthenticationException {
                if (username.equals("chrishent.mutondo@unza.zm")) {
                    User user = new User();
                    user.setUsername("012744");
                    user.setEmail("chrishent.mutondo@unza.zm");
                    user.setFirstName("Christopher");
                    user.setLastName("Mutondo");
                    user.setDepartment("Student Services");
                    return user;
                }
                if (username.equals("chrishentmatakala@yahoo.com")) {
                    User user = new User();
                    user.setUsername("CHRISTOPHER.HENTMAKALA");
                    user.setEmail("chrishentmatakala@yahoo.com");
                    user.setFirstName("Christopher");
                    user.setLastName("Hentmakala");
                    user.setDepartment("Student Services");
                    return user;
                }
                throw new ExternalAuthenticationException("User not found");
            }
        };
    }

    // Stub SIS Authentication Service for development
    @Bean("sisAuthenticationService")
    public ExternalAuthenticationService sisAuthenticationServiceStub() {
        logger.warn("Creating stub for SisAuthenticationService in development mode");
        return new ExternalAuthenticationService() {
            @Override
            public ExternalAuthResponse authenticate(String username, String password)
                    throws ExternalAuthenticationException {
                logger.debug("Stub SIS Authentication attempt for: {}", username);

                // Test credentials for development
                if (username.equals("2022531784") && password.equals("2022531784")) {
                    logger.info("Stub SIS authentication successful for: {}", username);
                    User stubUser = new User();
                    stubUser.setUsername("2022531784");
                    stubUser.setEmail("2022531784@unza.zm");
                    stubUser.setFirstName("Test");
                    stubUser.setLastName("Student");
                    stubUser.setDepartment("Agricultural Sciences");
                    stubUser.setProgram("Bachelor of Science Human Nutrition");
                    stubUser.setYearOfStudy(2);
                    stubUser.setStudentId("2022531784");
                    stubUser.setActive(true);
                    stubUser.setEmailVerified(true);
                    stubUser.setAuthenticationSource(AuthenticationSource.SIS);
                    stubUser.setCreatedAt(LocalDateTime.now());
                    stubUser.setUpdatedAt(LocalDateTime.now());
                    stubUser.setPassword("EXTERNALLY_AUTHENTICATED_USER_NO_PASSWORD");
                    stubUser.setRoles(new HashSet<>());

                    return new ExternalAuthResponse(true, "Authentication successful", stubUser, "2022531784", "SIS");
                }

                logger.warn("Stub SIS authentication failed for: {}", username);
                return new ExternalAuthResponse(false, "Invalid student credentials");
            }

            @Override
            public boolean validateUserExists(String username) {
                return username.equals("2022531784");
            }

            @Override
            public User getUserDetails(String username) throws ExternalAuthenticationException {
                throw new ExternalAuthenticationException("Not implemented in stub");
            }
        };
    }

    public RiskAssessmentService riskAssessmentService() {
        logger.warn(
                "Creating stub for RiskAssessmentService to allow application startup. Real implementation appears to be missing.");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(RiskAssessmentService.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            logger.debug("Invoked stubbed method on RiskAssessmentService: {}", method.getName());
            if (method.getReturnType().equals(boolean.class))
                return false;
            if (method.getReturnType().equals(int.class))
                return 0;
            if (method.getReturnType().equals(long.class))
                return 0L;
            return null;
        });
        return (RiskAssessmentService) enhancer.create();
    }

    @Bean
    @ConditionalOnMissingBean(NotificationService.class)
    public NotificationService notificationService() {
        logger.warn(
                "Creating stub for NotificationService to allow application startup. Real implementation appears to be missing.");
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
        logger.warn(
                "Creating stub for SimpMessagingTemplate to allow application startup. Real implementation appears to be missing.");

        // Create a dummy MessageChannel required for the SimpMessagingTemplate
        // constructor
        MessageChannel channelStub = (MessageChannel) Proxy.newProxyInstance(
                MessageChannel.class.getClassLoader(),
                new Class[] { MessageChannel.class },
                (proxy, method, args) -> true);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SimpMessagingTemplate.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            logger.debug("Invoked stubbed method on SimpMessagingTemplate: {}", method.getName());
            return null;
        });
        return (SimpMessagingTemplate) enhancer.create(new Class[] { MessageChannel.class },
                new Object[] { channelStub });
    }
}