package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.request.LoginRequest;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.AuthResponse;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.RoleRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.security.AuthenticationSource;
import zm.unza.counseling.security.external.ExternalAuthResponse;
import zm.unza.counseling.security.external.ExternalAuthenticationException;
import zm.unza.counseling.security.external.ExternalAuthenticationService;
import zm.unza.counseling.service.JwtService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Multi-Source Authentication Service
 * Handles authentication for students (SIS), staff (HR), and internal users (counselors/admin)
 * Enhanced with timeout handling and fallback mechanisms
 */
@Service
@RequiredArgsConstructor
public class MultiSourceAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Qualifier("sisAuthenticationService")
    private final ExternalAuthenticationService sisAuthenticationService;

    @Qualifier("hrAuthenticationService")
    private final ExternalAuthenticationService hrAuthenticationService;

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();

        System.out.println("==========================================");
        System.out.println("Login Request Debug Info:");
        System.out.println("Identifier: " + identifier);
        System.out.println("==========================================");

        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ValidationException("Identifier is required");
        }
    
        // Special handling for admin
        if ("admin@unza.zm".equals(identifier)) {
            System.out.println("Detected admin login - Taking INTERNAL authentication path");
            return authenticateInternal(request);
        }
    
        // Check if user exists locally and is explicitly INTERNAL
        // This allows admins with @unza.zm emails to login internally
        Optional<User> existingUser = userRepository.findByEmail(identifier);

        System.out.println("Checking for existing user by email: " + identifier + " - Found: " + existingUser.isPresent());

        // Fallback: Check by username if email lookup failed
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByUsername(identifier);
            System.out.println("Checking for existing user by username: " + identifier + " - Found: " + existingUser.isPresent());
        }

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            System.out.println("User found - Email: " + user.getEmail() + ", Username: " + user.getUsername() + ", AuthSource: " + user.getAuthenticationSource() + ", Active: " + user.getActive());
            if (user.getAuthenticationSource() == AuthenticationSource.INTERNAL) {
                System.out.println("Detected existing INTERNAL user - Taking INTERNAL authentication path");
                return authenticateInternal(request);
            } else {
                System.out.println("User exists but not INTERNAL - AuthSource: " + user.getAuthenticationSource());
            }
        } else {
            System.out.println("No existing user found for identifier: " + identifier);
        }

        // Detect user type from identifier
        if (isNumeric(identifier)) {
            System.out.println("Detected numeric identifier - Taking SIS authentication path");
            return authenticateStudent(request);
        } else if (identifier.toLowerCase().contains("@unza.zm")) {
            System.out.println("Detected @unza.zm email - Taking HR authentication path");
            return authenticateStaff(request);
        } else {
            System.out.println("Defaulting to INTERNAL authentication path");
            return authenticateInternal(request);
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already in use");
        }

        User user;
        Role.ERole roleEnum = null;

        if (request.getRole() != null) {
            try {
                roleEnum = Role.ERole.valueOf(request.getRole());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid role specified");
            }
        }

        if (roleEnum == Role.ERole.ROLE_ADMIN) {
            user = new zm.unza.counseling.entity.Admin();
        } else if (roleEnum == Role.ERole.ROLE_COUNSELOR) {
            user = new zm.unza.counseling.entity.Counselor();
        } else {
            user = new User();
        }

        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail()); // Use email as username for internal users
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setEmailVerified(false); // Email not verified on registration
        user.setAuthenticationSource(AuthenticationSource.INTERNAL);

        Set<Role> roles = new HashSet<>();
        if (roleEnum != null) {
            Role userRole = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new ValidationException("Invalid role specified"));
            roles.add(userRole);
        } else {
            // Default to STUDENT role if none is provided
            Role userRole = roleRepository.findByName(Role.ERole.ROLE_STUDENT)
                    .orElseThrow(() -> new ValidationException("Default role not found"));
            roles.add(userRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return createAuthResponse(savedUser);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ValidationException("User not found for refresh token"));

        if (jwtService.isTokenValid(refreshToken, user)) {
            return createAuthResponse(user);
        }
        throw new ValidationException("Invalid refresh token");
    }

    /**
     * Request password reset
     * @param email the user email
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found with email: " + email));
        
        // Generate reset token and send email
        // Implementation would depend on your email service
    }

    /**
     * Reset password
     * @param token the reset token
     * @param newPassword the new password
     */
    public void resetPassword(String token, String newPassword) {
        // Validate token and reset password
        // Implementation would depend on your token validation logic
    }

    /**
     * Verify email
     * @param token the verification token
     */
    public void verifyEmail(String token) {
        // Verify email using token
        // Implementation would depend on your token validation logic
    }

    /**
     * Resend verification email
     * @param email the user email
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found with email: " + email));
        
        // Send verification email
        // Implementation would depend on your email service
    }

    /**
     * Validate token
     * @param token the token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        // Validate token
        // Implementation would depend on your token validation logic
        return false;
    }

    private AuthResponse authenticateStudent(LoginRequest request) {
        try {
            ExternalAuthResponse externalResponse = sisAuthenticationService.authenticate(request.getIdentifier(), request.getPassword());

            if (externalResponse.isAuthenticated()) {
                User provisionedUser = provisionUser(externalResponse.getUser());
                return createAuthResponse(provisionedUser);
            } else {
                throw new ValidationException(externalResponse.getMessage() != null ? externalResponse.getMessage() : "Invalid student credentials");
            }
        } catch (ExternalAuthenticationException e) {
            // Log the exception in a real application
            System.err.println("SIS Authentication failed: " + e.getMessage());
            throw new ValidationException("SIS Authentication failed: " + e.getMessage());
        }
    }

    private AuthResponse authenticateStaff(LoginRequest request) {
        try {
            ExternalAuthResponse externalResponse = hrAuthenticationService.authenticate(request.getIdentifier(), request.getPassword());

            if (externalResponse.isAuthenticated()) {
                User provisionedUser = provisionUser(externalResponse.getUser());
                return createAuthResponse(provisionedUser);
            } else {
                throw new ValidationException(externalResponse.getMessage() != null ? externalResponse.getMessage() : "Invalid staff credentials");
            }
        } catch (ExternalAuthenticationException e) {
            // Enhanced error handling for timeout scenarios
            if (e.getMessage() != null && (e.getMessage().contains("temporarily unavailable") ||
                e.getMessage().contains("not accessible") ||
                e.getMessage().contains("taking too long"))) {
                // For timeout scenarios, provide a more user-friendly message
                System.err.println("HR system timeout - providing fallback authentication mechanism");
                // In a real scenario, you might want to implement a fallback mechanism here
                // For now, we'll return a specific error message
                throw new ValidationException("Staff authentication system is temporarily unavailable. Please try again later or contact support.");
            }
            
            // Log the exception in a real application
            System.err.println("HR Authentication failed: " + e.getMessage());
            throw new ValidationException("HR Authentication failed: " + e.getMessage());
        }
    }

    private AuthResponse authenticateInternal(LoginRequest request) {
        // Internal authentication using database
        return authenticateAgainstDatabase(request, AuthenticationSource.INTERNAL);
    }

    private AuthResponse authenticateAgainstDatabase(LoginRequest request, AuthenticationSource source) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Try finding by email first, then fallback to username
            User user = userRepository.findByEmailWithRoles(request.getIdentifier())
                    .or(() -> userRepository.findByUsername(request.getIdentifier()))
                    .orElseThrow(() -> new ValidationException("Invalid email or password"));

            System.out.println("==========================================");
            System.out.println("Internal User Debug Info:");
            System.out.println("Identifier: " + request.getIdentifier());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Authentication Source: " + user.getAuthenticationSource());
            System.out.println("Roles: " + user.getRoles());
            System.out.println("Email Verified: " + user.getEmailVerified());
            System.out.println("==========================================");

            // Verify the authentication source matches
            if (user.getAuthenticationSource() != source) {
                throw new ValidationException("Authentication source mismatch");
            }

            return createAuthResponse(user);
        } catch (BadCredentialsException e) {
            throw e;
        } catch (AuthenticationException e) {
            // Catch other auth exceptions (like InternalAuthenticationServiceException) and treat as BadCredentials
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            // Catch specific Spring Security exceptions for better messages
            throw new ValidationException("Internal authentication failed: Invalid credentials.");
        }
    }

    /**
     * Provisions a user from an external source.
     * If the user exists (by username), it updates their details.
     * If not, it creates a new user record.
     * The user's password is not stored for external users. A placeholder is used.
     *
     * @param externalUser The user object from the external authentication service.
     * @return The managed (persisted) User entity.
     */
    private User provisionUser(User externalUser) {
        if (externalUser == null || externalUser.getUsername() == null || externalUser.getUsername().isBlank()) {
            throw new ValidationException("External user data is invalid or missing a username.");
        }

        Optional<User> existingUserOpt = userRepository.findByUsername(externalUser.getUsername());

        User userToSave;
        if (existingUserOpt.isPresent()) {
            // User exists, update their details
            userToSave = existingUserOpt.get();
            userToSave.setFirstName(externalUser.getFirstName());
            userToSave.setLastName(externalUser.getLastName());
            userToSave.setEmail(externalUser.getEmail());
            userToSave.setPhoneNumber(externalUser.getPhoneNumber());
            userToSave.setDepartment(externalUser.getDepartment());
            userToSave.setProgram(externalUser.getProgram());
            userToSave.setYearOfStudy(externalUser.getYearOfStudy());
            userToSave.setGender(externalUser.getGender());
            userToSave.setDateOfBirth(externalUser.getDateOfBirth());
            if (externalUser.getStudentId() != null) {
                userToSave.setStudentId(externalUser.getStudentId());
            }
            userToSave.setUpdatedAt(LocalDateTime.now());
        } else {
            // New user, create a record
            Client client = new Client();
            client.setRiskLevel(Client.RiskLevel.LOW);
            client.setRegistrationDate(LocalDateTime.now());
            // Set initial status to indicate consent is needed.
            // The frontend will check this status in the returned user object to show the consent form.
            client.setClientStatus(Client.ClientStatus.ACTIVE);
            userToSave = client;
            userToSave.setUsername(externalUser.getUsername());
            userToSave.setFirstName(externalUser.getFirstName());
            userToSave.setLastName(externalUser.getLastName());
            userToSave.setEmail(externalUser.getEmail());
            userToSave.setPhoneNumber(externalUser.getPhoneNumber());
            userToSave.setDepartment(externalUser.getDepartment());
            userToSave.setProgram(externalUser.getProgram());
            userToSave.setYearOfStudy(externalUser.getYearOfStudy());
            userToSave.setGender(externalUser.getGender());
            userToSave.setDateOfBirth(externalUser.getDateOfBirth());
            if (externalUser.getStudentId() != null) {
                userToSave.setStudentId(externalUser.getStudentId());
            }
            userToSave.setCreatedAt(LocalDateTime.now());
            userToSave.setUpdatedAt(LocalDateTime.now());
            // External users don't have a password stored in our system.
            // A non-null password is required for the UserDetails contract, but it won't be used for login.
            userToSave.setPassword("EXTERNALLY_AUTHENTICATED_USER_NO_PASSWORD");
        }

        // Map Client specific fields if applicable
        if (userToSave instanceof Client) {
            Client client = (Client) userToSave;
            client.setProgramme(externalUser.getProgram());
            client.setFaculty(externalUser.getDepartment());
            if (externalUser.getStudentId() != null) {
                client.setStudentId(externalUser.getStudentId());
            }
        }

        // Map roles from external user
        if (externalUser.getRoles() != null && !externalUser.getRoles().isEmpty()) {
            Set<Role> userRoles = userToSave.getRoles();
            if (userRoles == null) {
                userRoles = new HashSet<>();
            }

            for (Role extRole : externalUser.getRoles()) {
                Role dbRole = roleRepository.findByName(extRole.getName())
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName(extRole.getName());
                            newRole.setDescription("Provisioned from " + externalUser.getAuthenticationSource());
                            return roleRepository.save(newRole);
                        });
                userRoles.add(dbRole);
            }
            userToSave.setRoles(userRoles);
        }

        // Ensure HR users don't have STUDENT role (cleanup for existing users)
        if (externalUser.getAuthenticationSource() == AuthenticationSource.HR && userToSave.getRoles() != null) {
            userToSave.getRoles().removeIf(r -> r.getName() == Role.ERole.ROLE_STUDENT);
        }

        // Handle email for external users - generate default if missing
        if (userToSave.getEmail() == null || userToSave.getEmail().trim().isEmpty()) {
            if (userToSave.getStudentId() != null && !userToSave.getStudentId().trim().isEmpty()) {
                // Generate email from student ID
                userToSave.setEmail(userToSave.getStudentId().toLowerCase() + "@unza.zm");
            } else if (userToSave.getUsername() != null && !userToSave.getUsername().trim().isEmpty()) {
                // Generate email from username
                userToSave.setEmail(userToSave.getUsername().toLowerCase() + "@unza.zm");
            } else {
                // Fallback - use a generic email format
                userToSave.setEmail("user" + System.currentTimeMillis() + "@unza.zm");
            }
        }

        // Ensure authentication source is set correctly from the external source
        userToSave.setAuthenticationSource(externalUser.getAuthenticationSource());
        
        // Set default role if none exists, but preserve existing roles for internal users
        if (userToSave.getRoles() == null || userToSave.getRoles().isEmpty()) {
            Role.ERole defaultRoleType;
            String roleDescription;

            if (userToSave.getAuthenticationSource() == AuthenticationSource.HR) {
                // For HR staff, determine role based on position/department
                // Check if staff member should be a counselor based on their position
                String position = userToSave.getDepartment(); // Position is stored in department field
                System.out.println("HR User Position/Department: " + position);
                
                if (position != null && (position.toLowerCase().contains("counselor") ||
                    position.toLowerCase().contains("counsellor") ||
                    position.toLowerCase().contains("psychologist") ||
                    position.toLowerCase().contains("therapist") ||
                    position.toLowerCase().contains("mental health") ||
                    position.toLowerCase().contains("guidance") ||
                    position.toLowerCase().contains("student services") ||
                    position.toLowerCase().contains("wellness"))) {
                    defaultRoleType = Role.ERole.ROLE_COUNSELOR;
                    roleDescription = "Counselor role for HR staff based on position";
                } else {
                    // Default to counselor role for HR staff (since they're logging in as staff/counselors)
                    defaultRoleType = Role.ERole.ROLE_COUNSELOR;
                    roleDescription = "Counselor role for HR staff members";
                }
            } else if (userToSave.getAuthenticationSource() == AuthenticationSource.INTERNAL) {
                // For internal users, determine role based on their entity type
                if (userToSave instanceof zm.unza.counseling.entity.Admin) {
                    defaultRoleType = Role.ERole.ROLE_ADMIN;
                    roleDescription = "Admin role for internal admin users";
                } else if (userToSave instanceof zm.unza.counseling.entity.Counselor) {
                    defaultRoleType = Role.ERole.ROLE_COUNSELOR;
                    roleDescription = "Counselor role for internal counselor users";
                } else {
                    // For regular User entities (like those created by InitialAdminConfig),
                    // default to client role
                    defaultRoleType = Role.ERole.ROLE_CLIENT;
                    roleDescription = "Client role for internal users";
                }
            } else {
                defaultRoleType = Role.ERole.ROLE_STUDENT;
                roleDescription = "Student role for external users";
            }
            
            Role defaultRole = roleRepository.findByName(defaultRoleType)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(defaultRoleType);
                        newRole.setDescription(roleDescription);
                        return roleRepository.save(newRole);
                    });
            
            Set<Role> roles = new HashSet<>();
            roles.add(defaultRole);
            userToSave.setRoles(roles);
        } else if (userToSave.getAuthenticationSource() == AuthenticationSource.INTERNAL) {
            // For existing internal users, preserve their existing roles
            // Don't overwrite roles for internal users who already have them
            System.out.println("Preserving existing roles for internal user: " + userToSave.getUsername());
        }
        
        userToSave.setActive(true);
        userToSave.setEmailVerified(true); // Assume verified from external source
        
        try {
            return userRepository.save(userToSave);
        } catch (Exception e) {
            System.err.println("Error provisioning user: " + e.getMessage());
            throw new ValidationException("Failed to provision user: " + e.getMessage());
        }
    }

    private AuthResponse createAuthResponse(User user) {
        // Ensure roles are loaded before creating the token and response
        User userWithRoles = userRepository.findByEmailWithRoles(user.getEmail())
                .or(() -> userRepository.findByUsername(user.getUsername()))
                .orElseThrow(() -> new ValidationException("User not found when creating auth response"));

        String token = jwtService.generateToken(userWithRoles);
        String refreshToken = jwtService.generateRefreshToken(userWithRoles);

        // Update last login time
        userWithRoles.setLastLogin(LocalDateTime.now());
        userRepository.save(userWithRoles);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(userWithRoles);
        response.setExpiresIn((int) jwtService.getExpirationTime());

        return response;
    }
}