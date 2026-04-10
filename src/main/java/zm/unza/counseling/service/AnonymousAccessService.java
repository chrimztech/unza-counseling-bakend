package zm.unza.counseling.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.AnonymousLoginRequest;
import zm.unza.counseling.dto.response.AuthResponse;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.RoleRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.security.AuthenticationSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AnonymousAccessService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AnonymousAccessService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public AuthResponse authenticate(AnonymousLoginRequest request, String ipAddress, String userAgent) {
        String deviceIdentifier = normalizeRequired(request.getDeviceIdentifier(), "Device identifier is required");
        String identifierHash = hash(deviceIdentifier);
        String displayName = sanitizeDisplayName(request.getDisplayName());

        User user = userRepository.findByAnonymousIdentifierHash(identifierHash).orElse(null);
        boolean created = user == null;
        if (created) {
            user = buildAnonymousUser(identifierHash, displayName);
        }

        applyAnonymousMetadata(user, identifierHash, displayName);
        user = userRepository.save(user);

        auditLogService.logAction(
                created ? "ANONYMOUS_SESSION_CREATED" : "ANONYMOUS_SESSION_RESUMED",
                "ANONYMOUS_USER",
                String.valueOf(user.getId()),
                buildAuditDetails(user, userAgent),
                String.valueOf(user.getId()),
                ipAddress,
                true
        );

        return createAuthResponse(user);
    }

    private User buildAnonymousUser(String identifierHash, String displayName) {
        Role clientRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT)
                .orElseThrow(() -> new ValidationException("ROLE_CLIENT is not configured"));

        User user = new User();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String email = "anonymous+" + suffix + "@counseling.unza.local";
        user.setUsername("anonymous_" + suffix);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRoles(Set.of(clientRole));
        user.setCreatedAt(LocalDateTime.now());
        user.setAvailableForAppointments(false);
        applyAnonymousMetadata(user, identifierHash, displayName);
        return user;
    }

    private void applyAnonymousMetadata(User user, String identifierHash, String displayName) {
        LocalDateTime now = LocalDateTime.now();
        String resolvedDisplayName = displayName != null ? displayName : user.getAnonymousDisplayName();
        if (resolvedDisplayName == null || resolvedDisplayName.isBlank()) {
            resolvedDisplayName = "Anonymous User";
        }

        String[] names = splitName(resolvedDisplayName);
        user.setFirstName(names[0]);
        user.setLastName(names[1]);
        user.setAnonymous(true);
        user.setAnonymousIdentifierHash(identifierHash);
        user.setAnonymousDisplayName(resolvedDisplayName);
        user.setAuthenticationSource(AuthenticationSource.ANONYMOUS);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setHasSignedConsent(true);
        user.setLastAnonymousActivityAt(now);
        user.setLastLogin(now);
        user.setUpdatedAt(now);
    }

    private AuthResponse createAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(user);
        response.setExpiresIn((int) jwtService.getExpirationTime());
        response.setFirstLogin(false);
        response.setRequiresConsent(false);
        return response;
    }

    private String buildAuditDetails(User user, String userAgent) {
        StringBuilder details = new StringBuilder("Anonymous counseling access established");
        if (user.getAnonymousDisplayName() != null && !user.getAnonymousDisplayName().isBlank()) {
            details.append(" for ").append(user.getAnonymousDisplayName());
        }
        if (userAgent != null && !userAgent.isBlank()) {
            details.append(" | userAgent=").append(userAgent);
        }
        return details.toString();
    }

    private String sanitizeDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        String sanitized = displayName.trim().replaceAll("\\s+", " ");
        if (sanitized.isEmpty()) {
            return null;
        }
        return sanitized.length() > 100 ? sanitized.substring(0, 100) : sanitized;
    }

    private String normalizeRequired(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
        return value.trim();
    }

    private String[] splitName(String displayName) {
        String normalized = displayName == null || displayName.isBlank() ? "Anonymous User" : displayName;
        String[] parts = normalized.split(" ", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], "User"};
        }
        return new String[]{parts[0], parts[1]};
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
