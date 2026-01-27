package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.AnonymousLoginRequest;
import zm.unza.counseling.dto.request.LoginRequest;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.AuthResponse;
import zm.unza.counseling.service.MultiSourceAuthService;
import zm.unza.counseling.service.ConsentFormService;
import zm.unza.counseling.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/auth", "/auth"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final MultiSourceAuthService multiSourceAuthService;
    private final ConsentFormService consentFormService;

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
            // Log this in production (e.g., using SLF4J)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred during login: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = multiSourceAuthService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
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
    public ResponseEntity<?> anonymousLogin(@Valid @RequestBody AnonymousLoginRequest request) {
        try {
            // For anonymous login, we need to create a temporary user or use a special anonymous user
            // This is a simplified implementation - in production, you might want to:
            // 1. Create a temporary anonymous user
            // 2. Generate a special anonymous JWT token
            // 3. Track the device/session for future reference
            
            // For now, we'll return a special response indicating anonymous access
            return ResponseEntity.ok(Map.of(
                "message", "Anonymous login successful",
                "anonymous", true,
                "deviceIdentifier", request.getDeviceIdentifier()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Anonymous login failed"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        // TODO: Implement proper authenticated user profile retrieval
        return ResponseEntity.ok().body(Map.of("message", "Profile endpoint - implement authenticated user details"));
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

    // Missing authentication endpoints

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices() {
        // TODO: Implement logout from all devices functionality
        return ResponseEntity.ok().body(Map.of("message", "Logout from all devices - implement token blacklisting"));
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
        // TODO: Implement user permissions retrieval
        return ResponseEntity.ok().body(Map.of("permissions", "implement permissions retrieval"));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enableTwoFactorAuth() {
        // TODO: Implement 2FA enable functionality
        return ResponseEntity.ok().body(Map.of("message", "2FA enable - implement TOTP setup"));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disableTwoFactorAuth() {
        // TODO: Implement 2FA disable functionality
        return ResponseEntity.ok().body(Map.of("message", "2FA disable - implement TOTP removal"));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFactorAuth(@RequestParam String code) {
        // TODO: Implement 2FA verification functionality
        return ResponseEntity.ok().body(Map.of("message", "2FA verify - implement TOTP verification"));
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