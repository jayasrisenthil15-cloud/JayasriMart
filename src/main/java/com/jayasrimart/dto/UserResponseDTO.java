package com.jayasrimart.dto;

import com.jayasrimart.model.Role;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Data Transfer Object for user representations.
 * Employs the Builder design pattern.
 * <p>
 * IMPORTANT SECURITY REQUIREMENT: Never contains password or password hash fields.
 */
public class UserResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String name;
    private final String email;
    private final Role role;
    private final Timestamp createdAt;

    private UserResponseDTO(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.role = builder.role;
        this.createdAt = builder.createdAt != null ? new Timestamp(builder.createdAt.getTime()) : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Timestamp getCreatedAt() {
        return createdAt != null ? new Timestamp(createdAt.getTime()) : null;
    }

    public boolean isBuyer() {
        return Role.BUYER.equals(role);
    }

    public boolean isSeller() {
        return Role.SELLER.equals(role);
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserResponseDTO that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "UserResponseDTO{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", role=" + role
                + ", createdAt=" + createdAt
                + '}';
    }

    /**
     * Builder class for {@link UserResponseDTO}.
     */
    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private Timestamp createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder createdAt(Timestamp createdAt) {
            this.createdAt = createdAt != null ? new Timestamp(createdAt.getTime()) : null;
            return this;
        }

        public UserResponseDTO build() {
            return new UserResponseDTO(this);
        }
    }
}
