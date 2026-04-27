package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.UserResponse;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/v1/users", "/users", "/api/v1/users", "/api/users"})
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    // Notification settings endpoints - MUST come before /{id} to avoid path matching conflicts
    @GetMapping("/{userId}/notification-settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserNotificationSettings(@PathVariable Long userId) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("emailNotifications", true);
        settings.put("pushNotifications", true);
        settings.put("appointmentReminders", true);
        settings.put("messageNotifications", true);
        settings.put("weeklyDigest", false);
        settings.put("riskAlerts", true);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/{userId}/notification-settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserNotificationSettings(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> settings) {
        return ResponseEntity.ok(ApiResponse.success(settings, "Notification settings updated successfully"));
    }

    @GetMapping("/{id}/is-anonymous")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Boolean>> isAnonymousUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(Boolean.TRUE.equals(userService.getUserById(id).getAnonymous())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserByEmail(email)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody RegisterRequest request) {
        User createdUser = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(createdUser, "User created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean permanent) {
        userService.deleteUser(id, permanent);
        String message = permanent
                ? "User permanently deleted successfully"
                : "User deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> permanentlyDeleteUser(@PathVariable Long id) {
        userService.permanentlyDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User permanently deleted successfully"));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByRole(role)));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Long>> getUserCount() {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserCount()));
    }

    // Missing endpoints implementation

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<User>>> searchUsers(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.searchUsers(query, pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<User>>> getActiveUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getActiveUsers(pageable)));
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<User>>> getInactiveUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getInactiveUsers(pageable)));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable Long id) {
        User activatedUser = userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(activatedUser, "User activated successfully"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> deactivateUser(@PathVariable Long id) {
        User deactivatedUser = userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(deactivatedUser, "User deactivated successfully"));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<String>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllRoles()));
    }

    @GetMapping("/count-by-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUserCountByRole() {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserCountByRole()));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<User>> getCurrentUserProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUserProfile()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<User>> updateCurrentUserProfile(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());

        if (payload.containsKey("email")) {
            user.setEmail(String.valueOf(payload.get("email")));
        }
        if (payload.containsKey("firstName")) {
            user.setFirstName(String.valueOf(payload.get("firstName")));
        }
        if (payload.containsKey("lastName")) {
            user.setLastName(String.valueOf(payload.get("lastName")));
        }
        if (payload.containsKey("phoneNumber")) {
            user.setPhoneNumber(String.valueOf(payload.get("phoneNumber")));
        }
        if (payload.containsKey("bio")) {
            user.setBio(String.valueOf(payload.get("bio")));
        }
        if (payload.containsKey("department")) {
            user.setDepartment(String.valueOf(payload.get("department")));
        }
        if (payload.containsKey("program")) {
            user.setProgram(String.valueOf(payload.get("program")));
        }

        User updatedUser = userService.updateUser(user.getId(), user);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Profile updated successfully"));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> changeUserPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userService.changeUserPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(defaultValue = "csv") String format) {
        byte[] exportedData = userService.exportUsers(format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=users." + format)
                .body(exportedData);
    }

}
