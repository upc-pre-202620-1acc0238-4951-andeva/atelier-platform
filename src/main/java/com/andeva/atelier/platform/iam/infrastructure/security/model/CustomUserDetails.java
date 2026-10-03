package com.andeva.atelier.platform.iam.infrastructure.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Custom {@link UserDetails} implementation carrying domain identification,
 * multi-tenant context, and granted security authorities.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final UUID tenantId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public CustomUserDetails(
            UUID userId,
            String username,
            String password,
            UUID tenantId,
            Collection<? extends GrantedAuthority> authorities,
            boolean enabled) {
        this.userId = Objects.requireNonNull(userId, "User identifier cannot be null");
        this.username = Objects.requireNonNull(username, "Username/email cannot be null");
        this.password = password;
        this.tenantId = tenantId;
        this.authorities = authorities != null ? authorities : java.util.List.of();
        this.enabled = enabled;
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
}
