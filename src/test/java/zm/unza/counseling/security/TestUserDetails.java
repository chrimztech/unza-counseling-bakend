package zm.unza.counseling.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Test user details for integration testing.
 * Provides consistent test user authentication.
 */
public class TestUserDetails implements UserDetails {
    
    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    
    public TestUserDetails(UUID id, String username, String email, String password, 
                          Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }
    
    public static TestUserDetails createAdmin() {
        return new TestUserDetails(
            UUID.randomUUID(),
            "admin@test.com",
            "admin@test.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")),
            true
        );
    }
    
    public static TestUserDetails createCounselor() {
        return new TestUserDetails(
            UUID.randomUUID(),
            "counselor@test.com",
            "counselor@test.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_COUNSELOR")),
            true
        );
    }
    
    public static TestUserDetails createClient() {
        return new TestUserDetails(
            UUID.randomUUID(),
            "client@test.com",
            "client@test.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")),
            true
        );
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
}