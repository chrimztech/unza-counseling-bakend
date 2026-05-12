package zm.unza.counseling.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.AnonymousLoginRequest;
import zm.unza.counseling.dto.request.LoginRequest;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.AuthResponse;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.AnonymousAccessService;
import zm.unza.counseling.service.MultiSourceAuthService;
import zm.unza.counseling.service.ConsentFormService;
import zm.unza.counseling.service.TotpService;
import zm.unza.counseling.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth", "/auth"})
@CrossOrigin(origins = {"https://counselling.unza.ac.zm", "http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final MultiSourceAuthService multiSourceAuthService;
    private final ConsentFormService consentFormService;
    private final AnonymousAccessService anonymousAccessService;
    private final UserRepository userRepository;
    private final TotpService totpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = multiSourceAuthService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Handles invalid authenticationSource, missing fields logic, etc.
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid login request"));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            // Log the full exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = multiSourceAuthService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()  .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Client-side token removal is sufficient for JWT.
        // Server-side logout (e.g., blacklist token) can be added later.
        return ResponseEntity.ok().body(Map.of("message", "Logout successful"));
    }

    @PostMapping("/anonymous-login")
    public ResponseEntity<?> anonymousLogin(@Valid @RequestBody AnonymousLoginRequest request, HttpServletRequest httpRequest) {
        try {
            String ipAddress = request.getIpAddress();
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = httpRequest.getHeader("X-Forwarded-For");
            }
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = httpRequest.getRemoteAddr();
            }

            if (request.getUserAgent() == null || request.getUserAgent().isBlank()) {
                request.setUserAgent(httpRequest.getHeader("User-Agent"));
            }

            AuthResponse response = anonymousAccessService.authenticate(request, ipAddress, request.getUserAgent());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Anonymous login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("profilePicture", user.getProfilePicture());
        profile.put("bio", user.getBio());
        profile.put("gender", user.getGender());
        profile.put("dateOfBirth", user.getDateOfBirth());
        profile.put("department", user.getDepartment());
        profile.put("program", user.getProgram());
        profile.put("yearOfStudy", user.getYearOfStudy());
        profile.put("studentId", user.getStudentId());
        profile.put("active", user.getActive());
        profile.put("emailVerified", user.getEmailVerified());
        profile.put("twoFactorEnabled", Boolean.TRUE.equals(user.getTwoFactorEnabled()));
        profile.put("roles", user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));
        profile.put("createdAt", user.getCreatedAt());
        profile.put("lastLogin", user.getLastLogin());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "refreshToken is required"));
            }
            AuthResponse response = multiSourceAuthService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setTokenIssuedBefore(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "All sessions invalidated successfully. Please log in again on other devices."));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            multiSourceAuthService.requestPasswordReset(email);
            return ResponseEntity.ok().body(Map.of("message", "Password reset email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to send password reset email"));
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            multiSourceAuthService.resetPassword(token, newPassword);
            return ResponseEntity.ok().body(Map.of("message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired reset token"));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            multiSourceAuthService.verifyEmail(token);
            return ResponseEntity.ok().body(Map.of("message", "Email verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired verification token"));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam String email) {
        try {
            multiSourceAuthService.resendVerificationEmail(email);
            return ResponseEntity.ok().body(Map.of("message", "Verification email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to send verification email"));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            boolean isValid = multiSourceAuthService.validateToken(token);
            return ResponseEntity.ok().body(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", "Invalid token format"));
        }
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> getUserPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        var authorities = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "permissions", authorities,
                "roles", authorities.stream().filter(a -> a.startsWith("ROLE_")).collect(Collectors.toList())
        ));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enableTwoFactorAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return ResponseEntity.badRequest().body(Map.of("error", "2FA is already enabled"));
        }
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);
        String otpUri = totpService.buildOtpAuthUri(secret, user.getEmail(), "UNZA Counseling");
        return ResponseEntity.ok(Map.of(
                "secret", secret,
                "otpAuthUri", otpUri,
                "message", "Scan the QR code with your authenticator app, then call /2fa/verify with the 6-digit code to activate."
        ));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFactorAuth(@RequestParam String code) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getTotpSecret() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "2FA setup not initiated. Call /2fa/enable first."));
        }
        if (!totpService.verify(user.getTotpSecret(), code)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid 2FA code"));
        }
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "2FA enabled successfully"));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disableTwoFactorAuth(@RequestParam String code) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return ResponseEntity.badRequest().body(Map.of("error", "2FA is not enabled"));
        }
        if (!totpService.verify(user.getTotpSecret(), code)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid 2FA code"));
        }
        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "2FA disabled successfully"));
    }

    // Global exception handler for @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
