package zm.unza.counseling.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.LoginRequest;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.AuthResponse;
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
public class MultiSourceAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final ExternalAuthenticationService sisAuthenticationService;
    private final ExternalAuthenticationService hrAuthenticationService;

    public MultiSourceAuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            @Qualifier("sisAuthenticationService") ExternalAuthenticationService sisAuthenticationService,
            @Qualifier("hrAuthenticationService") ExternalAuthenticationService hrAuthenticationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.sisAuthenticationService = sisAuthenticationService;
        this.hrAuthenticationService = hrAuthenticationService;
    }

    @Transactional
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
        System.out.println("DEBUG: About to detect user type for identifier: " + identifier);
        if (isNumeric(identifier)) {
            System.out.println("DEBUG: Detected numeric identifier - Taking SIS authentication path");
            return authenticateStudent(request);
        } else if (identifier.toLowerCase().contains("@unza.zm") || 
                   identifier.toLowerCase().contains("@unza.ac.zm") || 
                   identifier.equalsIgnoreCase("chrishentmatakala@yahoo.com")) {
            System.out.println("DEBUG: Detected staff email - Taking HR authentication path");
            return authenticateStaff(request);
        } else {
            System.out.println("DEBUG: Defaulting to INTERNAL authentication path");
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
    }

    /**
     * Reset password
     * @param token the reset token
     * @param newPassword the new password
     */
    public void resetPassword(String token, String newPassword) {
        // Validate token and reset password
    }

    /**
     * Verify email
     * @param token the verification token
     */
    public void verifyEmail(String token) {
        // Verify email using token
    }

    /**
     * Resend verification email
     * @param email the user email
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found with email: " + email));
        
        // Send verification email
    }

    /**
     * Validate token
     * @param token the token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        return false;
    }

    private AuthResponse authenticateStudent(LoginRequest request) {
        try {
            System.out.println("=== STUDENT AUTHENTICATION START ===");
            System.out.println("Identifier: " + request.getIdentifier());
            
            ExternalAuthResponse externalResponse = sisAuthenticationService.authenticate(request.getIdentifier(), request.getPassword());

            System.out.println("SIS Response - Authenticated: " + externalResponse.isAuthenticated());
            System.out.println("SIS Response - Message: " + externalResponse.getMessage());
            
            if (externalResponse.isAuthenticated()) {
                User provisionedUser = provisionUser(externalResponse.getUser());
                System.out.println("Provisioned User - Username: " + provisionedUser.getUsername() + ", Email: " + provisionedUser.getEmail());
                return createAuthResponse(provisionedUser);
            } else {
                throw new ValidationException(externalResponse.getMessage() != null ? externalResponse.getMessage() : "Invalid student credentials");
            }
        } catch (ExternalAuthenticationException e) {
            String errorMsg = e.getMessage();
            System.err.println("SIS Authentication failed: " + errorMsg);
            
            // Provide user-friendly error messages based on the error type
            if (errorMsg != null && errorMsg.contains("500")) {
                throw new ValidationException("Student authentication service is temporarily unavailable. Please try again later or contact support if the problem persists.");
            } else if (errorMsg != null && errorMsg.contains("401")) {
                throw new ValidationException("Invalid student number or password. Please check your credentials and try again.");
            } else if (errorMsg != null && errorMsg.contains("404")) {
                throw new ValidationException("Student not found in the system. Please verify your student number.");
            } else if (errorMsg != null && (errorMsg.contains("timeout") || errorMsg.contains("Connection refused"))) {
                throw new ValidationException("Unable to connect to student authentication service. Please try again later.");
            }
            
            throw new ValidationException("Student authentication failed: " + (errorMsg != null ? errorMsg : "Unknown error"));
        }
    }

    private AuthResponse authenticateStaff(LoginRequest request) {
        try {
            ExternalAuthResponse externalResponse = hrAuthenticationService.authenticate(request.getIdentifier(), request.getPassword());

            if (externalResponse.isAuthenticated()) {
                User provisionedUser = provisionUser(externalResponse.getUser());
                return createAuthResponse(provisionedUser);
            } else {
                // Check for rate limiting message
                String message = externalResponse.getMessage();
                if (message != null && message.toLowerCase().contains("too many")) {
                    throw new ValidationException("HR system rate limit exceeded. Please wait a few minutes before trying again.");
                }
                throw new ValidationException(externalResponse.getMessage() != null ? externalResponse.getMessage() : "Invalid staff credentials");
            }
        } catch (ExternalAuthenticationException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("temporarily unavailable") ||
                errorMsg.contains("not accessible") ||
                errorMsg.contains("taking too long") ||
                errorMsg.contains("Too many"))) {
                System.err.println("HR system rate limit or timeout - providing user-friendly message");
                throw new ValidationException("HR system is busy or rate limited. Please wait a few minutes before trying again.");
            }
            
            System.err.println("HR Authentication failed: " + e.getMessage());
            throw new ValidationException("HR Authentication failed. Please check your credentials and try again.");
        }
    }

    private AuthResponse authenticateInternal(LoginRequest request) {
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
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            throw new ValidationException("Internal authentication failed: Invalid credentials.");
        }
    }

    /**
     * Simplified user provisioning - creates or updates user from external source.
     * Uses repository operations for proper transaction management.
     * 
     * @param externalUser The user from external authentication
     * @return The saved user entity
     */
    @Transactional
    public User provisionUser(User externalUser) {
        if (externalUser == null || externalUser.getUsername() == null) {
            throw new ValidationException("Invalid external user data");
        }

        // Check if user already exists by username or email
        Optional<User> existingOpt = userRepository.findByUsername(externalUser.getUsername());
        if (existingOpt.isEmpty() && externalUser.getEmail() != null) {
            existingOpt = userRepository.findByEmail(externalUser.getEmail());
        }
        
        User userToSave;
        boolean isNewUser = false;
        
        if (existingOpt.isPresent()) {
            userToSave = existingOpt.get();
        } else {
            userToSave = new User();
            isNewUser = true;
        }

        // Set basic fields - use null checks to avoid validation errors
        userToSave.setUsername(safeTrim(externalUser.getUsername()));
        
        // Handle firstName - ALWAYS update from external source if meaningful
        String firstName = safeTrim(externalUser.getFirstName());
        System.out.println("DEBUG provisionUser: external firstName='" + firstName + "', isNewUser=" + isNewUser);
        
        // Check if the firstName is a meaningful value (not "Student" placeholder)
        boolean isMeaningfulFirstName = firstName != null && !firstName.isEmpty() 
                && !firstName.equals("Student") && !firstName.equals("User");
        
        if (isMeaningfulFirstName) {
            userToSave.setFirstName(firstName);
            System.out.println("DEBUG provisionUser: Setting firstName to '" + firstName + "'");
        } else if (isNewUser) {
            userToSave.setFirstName("Student"); // Default for SIS users without first name
            System.out.println("DEBUG provisionUser: Setting default firstName 'Student' for new user");
        }
        // For existing users, only update if external source provides a meaningful value
        // This ensures we always get the latest name from SIS when it's available
        
        // Handle lastName - ALWAYS update from external source if meaningful
        String lastName = safeTrim(externalUser.getLastName());
        System.out.println("DEBUG provisionUser: external lastName='" + lastName + "'");
        
        // Check if the lastName is a meaningful value (not the username as placeholder)
        boolean isMeaningfulLastName = lastName != null && !lastName.isEmpty() 
                && !lastName.equals(externalUser.getUsername()) && !lastName.equals("User");
        
        if (isMeaningfulLastName) {
            userToSave.setLastName(lastName);
            System.out.println("DEBUG provisionUser: Setting lastName to '" + lastName + "'");
        } else if (isNewUser) {
            userToSave.setLastName(externalUser.getUsername() != null ? externalUser.getUsername() : "User");
            System.out.println("DEBUG provisionUser: Setting default lastName '" + userToSave.getLastName() + "' for new user");
        }
        // For existing users, only update if external source provides a meaningful value
        // This ensures we always get the latest name from SIS when it's available
        
        // Handle email - use username@unza.zm as default if null or empty
        String email = safeTrim(externalUser.getEmail());
        if (email == null || email.isEmpty()) {
            email = externalUser.getUsername() + "@unza.zm";
        } else {
            // Fix common email typos (comma to dot)
            if (email.contains(",")) {
                email = email.replace(",", ".");
            }
            // Validate email format
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                email = externalUser.getUsername() + "@unza.zm";
            }
        }
        userToSave.setEmail(email);
        
        userToSave.setPhoneNumber(safeTrim(externalUser.getPhoneNumber()));
        userToSave.setDepartment(safeTrim(externalUser.getDepartment()));
        userToSave.setProgram(safeTrim(externalUser.getProgram()));
        
        if (externalUser.getYearOfStudy() != null) {
            userToSave.setYearOfStudy(externalUser.getYearOfStudy());
        }
        
        if (externalUser.getGender() != null) {
            userToSave.setGender(externalUser.getGender());
        }
        
        if (externalUser.getDateOfBirth() != null) {
            userToSave.setDateOfBirth(externalUser.getDateOfBirth());
        }
        
        if (safeTrim(externalUser.getStudentId()) != null) {
            userToSave.setStudentId(externalUser.getStudentId());
        }

        userToSave.setAuthenticationSource(externalUser.getAuthenticationSource());
        userToSave.setActive(true);
        userToSave.setEmailVerified(true);
        userToSave.setUpdatedAt(LocalDateTime.now());
        
        if (isNewUser) {
            userToSave.setCreatedAt(LocalDateTime.now());
            userToSave.setPassword("EXTERNALLY_AUTHENTICATED");
        }

        // Only set roles for NEW users - never modify roles on existing managed entities
        if (isNewUser) {
            // Set roles based on authentication source for new users
            Set<Role> roles = new HashSet<>();
            
            if (externalUser.getAuthenticationSource() == AuthenticationSource.SIS) {
                // SIS users get STUDENT and CLIENT roles
                roleRepository.findByName(Role.ERole.ROLE_STUDENT).ifPresent(roles::add);
                roleRepository.findByName(Role.ERole.ROLE_CLIENT).ifPresent(roles::add);
            } else if (externalUser.getAuthenticationSource() == AuthenticationSource.HR) {
                // HR users get CLIENT role by default
                roleRepository.findByName(Role.ERole.ROLE_CLIENT).ifPresent(roles::add);
            }
            
            userToSave.setRoles(roles);
        }
        
        // Save using repository (handles both insert and update)
        userToSave = userRepository.save(userToSave);
        
        // Note: Client record creation is disabled because Client extends User with SINGLE_TABLE
        // inheritance, which means they share the same table. Creating a separate Client record
        // would cause unique constraint violations on username/email columns.
        // The User entity already contains all necessary student information.
        
        System.out.println("Provisioned user: " + userToSave.getUsername() + " (isNewUser=" + isNewUser + ")");
        
        return userToSave;
    }
    
    /**
     * Safely trim a string, returning null if empty
     */
    private String safeTrim(String value) {
        if (value == null || "null".equalsIgnoreCase(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AuthResponse createAuthResponse(User user) {
        // Use the provided user directly - it's already loaded with roles from provisionUser
        // Re-fetching from database can cause issues within the same transaction
        User userForToken = user;
        
        // Only re-fetch if roles are not loaded (lazy loading check)
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            userForToken = userRepository.findByEmailWithRoles(user.getEmail())
                    .or(() -> userRepository.findByUsernameWithRoles(user.getUsername()))
                    .orElse(user);
        }

        String token = jwtService.generateToken(userForToken);
        String refreshToken = jwtService.generateRefreshToken(userForToken);

        // Check if this is the user's first login (lastLogin is null)
        boolean isFirstLogin = userForToken.getLastLogin() == null;

        // Update last login time
        userForToken.setLastLogin(LocalDateTime.now());
        userRepository.save(userForToken);

        // Check if user requires consent (clients/students/staff who haven't signed)
        boolean requiresConsent = isClientRequiringConsent(userForToken);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(userForToken);
        response.setExpiresIn((int) jwtService.getExpirationTime());
        response.setFirstLogin(isFirstLogin);
        response.setRequiresConsent(requiresConsent);

        return response;
    }

    /**
     * Check if user is a client (student/staff) who needs to sign consent form
     * @param user the user to check
     * @return true if user requires consent, false otherwise
     */
    private boolean isClientRequiringConsent(User user) {
        // Check if user has CLIENT role (students and staff)
        boolean isClient = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.ERole.ROLE_CLIENT || 
                                  role.getName() == Role.ERole.ROLE_STUDENT);
        
        if (!isClient) {
            // Not a client, no consent required
            return false;
        }
        
        // Check if user has already signed consent
        return !Boolean.TRUE.equals(user.getHasSignedConsent());
    }
}
