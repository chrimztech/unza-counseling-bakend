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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

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
            ExternalAuthResponse externalResponse = sisAuthenticationService.authenticate(request.getIdentifier(), request.getPassword());

            if (externalResponse.isAuthenticated()) {
                User provisionedUser = provisionUser(externalResponse.getUser());
                return createAuthResponse(provisionedUser);
            } else {
                throw new ValidationException(externalResponse.getMessage() != null ? externalResponse.getMessage() : "Invalid student credentials");
            }
        } catch (ExternalAuthenticationException e) {
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
            if (e.getMessage() != null && (e.getMessage().contains("temporarily unavailable") ||
                e.getMessage().contains("not accessible") ||
                e.getMessage().contains("taking too long"))) {
                System.err.println("HR system timeout - providing fallback authentication mechanism");
                throw new ValidationException("Staff authentication system is temporarily unavailable. Please try again later or contact support.");
            }
            
            System.err.println("HR Authentication failed: " + e.getMessage());
            throw new ValidationException("HR Authentication failed: " + e.getMessage());
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
     * This method is intentionally simple to avoid transaction issues.
     * 
     * @param externalUser The user from external authentication
     * @return The saved user entity
     */
    public User provisionUser(User externalUser) {
        if (externalUser == null || externalUser.getUsername() == null) {
            throw new ValidationException("Invalid external user data");
        }

        // Check if user already exists
        Optional<User> existingOpt = userRepository.findByUsername(externalUser.getUsername());
        
        User userToSave;
        boolean isNewUser = false;
        
        if (existingOpt.isPresent()) {
            userToSave = existingOpt.get();
        } else {
            userToSave = new User();
            isNewUser = true;
        }

        // Set basic fields
        userToSave.setUsername(externalUser.getUsername());
        userToSave.setFirstName(externalUser.getFirstName());
        userToSave.setLastName(externalUser.getLastName());
        
        // Handle email - use username@unza.zm as default if null
        String email = externalUser.getEmail();
        if (email == null || email.trim().isEmpty() || "null".equalsIgnoreCase(email)) {
            email = externalUser.getUsername() + "@unza.zm";
        }
        userToSave.setEmail(email);
        
        userToSave.setPhoneNumber(externalUser.getPhoneNumber());
        userToSave.setDepartment(externalUser.getDepartment());
        userToSave.setProgram(externalUser.getProgram());
        userToSave.setYearOfStudy(externalUser.getYearOfStudy());
        userToSave.setGender(externalUser.getGender());
        userToSave.setDateOfBirth(externalUser.getDateOfBirth());
        
        if (externalUser.getStudentId() != null) {
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
                Role studentRole = roleRepository.findByName(Role.ERole.ROLE_STUDENT).orElse(null);
                if (studentRole != null) {
                    roles.add(studentRole);
                }
                Role clientRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT).orElse(null);
                if (clientRole != null) {
                    roles.add(clientRole);
                }
            } else if (externalUser.getAuthenticationSource() == AuthenticationSource.HR) {
                // HR users get CLIENT role by default
                Role clientRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT).orElse(null);
                if (clientRole != null) {
                    roles.add(clientRole);
                }
            }
            
            userToSave.setRoles(roles);
        }

        // For existing users, use native UPDATE query to avoid SINGLE_TABLE inheritance issues
        if (!isNewUser) {
            // Update user fields directly using native query
            String nativeSql = """
                UPDATE users SET 
                    first_name = :firstName, 
                    last_name = :lastName, 
                    email = :email,
                    phone_number = :phoneNumber,
                    department = :department,
                    program = :program,
                    year_of_study = :yearOfStudy,
                    gender = :gender,
                    date_of_birth = :dateOfBirth,
                    student_id = :studentId,
                    authentication_source = :authSource,
                    is_active = true,
                    email_verified = true,
                    updated_at = NOW()
                WHERE id = :id
                """;
            
            entityManager.createNativeQuery(nativeSql)
                .setParameter("firstName", externalUser.getFirstName())
                .setParameter("lastName", externalUser.getLastName())
                .setParameter("email", email)
                .setParameter("phoneNumber", externalUser.getPhoneNumber())
                .setParameter("department", externalUser.getDepartment())
                .setParameter("program", externalUser.getProgram())
                .setParameter("yearOfStudy", externalUser.getYearOfStudy())
                .setParameter("gender", externalUser.getGender() != null ? externalUser.getGender().name() : null)
                .setParameter("dateOfBirth", externalUser.getDateOfBirth())
                .setParameter("studentId", externalUser.getStudentId())
                .setParameter("authSource", externalUser.getAuthenticationSource().name())
                .setParameter("id", userToSave.getId())
                .executeUpdate();
            
            entityManager.flush();
            
            // Return the user with refreshed data
            return entityManager.find(User.class, userToSave.getId());
        }

        // For new users, persist normally
        if (isNewUser) {
            entityManager.persist(userToSave);
        }

        return userToSave;
    }

    private AuthResponse createAuthResponse(User user) {
        // Fetch user with roles loaded
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
