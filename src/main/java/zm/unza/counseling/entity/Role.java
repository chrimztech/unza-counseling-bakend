package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import zm.unza.counseling.security.UserPermission;

import java.util.HashSet;
import java.util.Set;

/**
 * Role Entity - Defines user roles and permissions
 */
@Entity
@Table(name = "roles")
@EqualsAndHashCode
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private ERole name;

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Set<UserPermission> permissions = new HashSet<>();

    // Default constructor
    public Role() {}

    // All args constructor
    public Role(Long id, ERole name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Constructor with enum only
    public Role(ERole name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ERole getName() { return name; }
    public void setName(ERole name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<UserPermission> getPermissions() { return permissions; }
    public void setPermissions(Set<UserPermission> permissions) { this.permissions = permissions; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Role role = new Role();

        public Builder id(Long id) {
            role.setId(id);
            return this;
        }

        public Builder name(ERole name) {
            role.setName(name);
            return this;
        }

        public Builder description(String description) {
            role.setDescription(description);
            return this;
        }

        public Builder permissions(Set<UserPermission> permissions) {
            role.setPermissions(permissions);
            return this;
        }

        public Role build() {
            return role;
        }
    }

    public enum ERole {
        ROLE_STUDENT,       // Students seeking counseling
        ROLE_COUNSELOR,     // Professional counselors
        ROLE_ADMIN,         // System administrators
        ROLE_SUPER_ADMIN,
        ROLE_CLIENT    // Super administrators with full access
    }
}