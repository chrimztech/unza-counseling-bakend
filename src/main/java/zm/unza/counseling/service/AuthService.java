package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.request.LoginRequest;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.AuthResponse;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(user);
        response.setExpiresIn((int) jwtService.getExpirationTime());

        return response;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);

        if (request.getRole() != null) {
            try {
                user.setRole(Role.ERole.valueOf(request.getRole()));
            } catch (IllegalArgumentException e) {
                user.setRole(Role.ERole.ROLE_STUDENT);
            }
        } else {
            user.setRole(Role.ERole.ROLE_STUDENT);
        }

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(savedUser);
        response.setExpiresIn((int) jwtService.getExpirationTime());

        return response;
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (jwtService.isTokenValid(refreshToken, user)) {
            String newToken = jwtService.generateToken(user);
            AuthResponse response = new AuthResponse();
            response.setToken(newToken);
            response.setRefreshToken(refreshToken);
            response.setUser(user);
            response.setExpiresIn((int) jwtService.getExpirationTime());
            return response;
        }
        throw new ValidationException("Invalid refresh token");
    }
}