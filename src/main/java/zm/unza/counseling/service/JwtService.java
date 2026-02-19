package zm.unza.counseling.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import zm.unza.counseling.config.JwtConfig;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service - Handles JWT token generation and validation
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtConfig jwtConfig;

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract a specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate token for user
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate token for User entity (convenience method)
     */
    public String generateToken(zm.unza.counseling.entity.User user) {
        return generateToken(createUserDetails(user));
    }

    /**
     * Generate token with extra claims
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtConfig.getExpiration());
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtConfig.getRefreshExpiration());
    }

    /**
     * Generate refresh token for User entity (convenience method)
     */
    public String generateRefreshToken(zm.unza.counseling.entity.User user) {
        return generateRefreshToken(createUserDetails(user));
    }

    /**
     * Build JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        try {
            log.debug("Building JWT token for user: {}", userDetails.getUsername());
            return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate token
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final Date expiration = extractExpiration(token);
            
            // Check that username matches AND token has not expired
            // Using lenient expiration check with 5 minute clock skew
            boolean isUsernameValid = username != null && username.equals(userDetails.getUsername());
            
            // Check expiration - token is valid if expiration is in the future
            // or within 5 minutes past (clock skew tolerance)
            Date now = new Date();
            boolean isNotExpired = expiration == null || 
                    !expiration.before(new Date(now.getTime() - (5 * 60 * 1000)));
            
            log.debug("Token validation - username: {}, isUsernameValid: {}, expiration: {}, isNotExpired: {}", 
                username, isUsernameValid, expiration, isNotExpired);
            
            return isUsernameValid && isNotExpired;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get signing key
     */
    private Key getSignInKey() {
        String secret = jwtConfig.getSecret();
        
        if (secret == null || secret.isEmpty()) {
            log.error("JWT secret is null or empty!");
            throw new IllegalStateException("JWT secret is not configured");
        }
        
        log.debug("JWT secret configured (length: {})", secret.length());
        
        // Always use UTF-8 bytes for the secret to avoid base64 decoding issues
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get expiration time in milliseconds
     */
    public long getExpirationTime() {
        return jwtConfig.getExpiration();
    }

    /**
     * Create UserDetails from User entity
     */
     private UserDetails createUserDetails(zm.unza.counseling.entity.User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toArray(String[]::new))
                .accountExpired(!user.getActive())
                .accountLocked(!user.getActive())
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }
}