package com.andeva.atelier.platform.iam.infrastructure.security.services;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantMembershipPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantMembershipPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Custom {@link UserDetailsService} implementation resolving security credentials
 * and granted authorities from IAM persistence repositories.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserPersistenceRepository userPersistenceRepository;
    private final TenantMembershipPersistenceRepository membershipPersistenceRepository;

    public CustomUserDetailsService(
            UserPersistenceRepository userPersistenceRepository,
            TenantMembershipPersistenceRepository membershipPersistenceRepository) {
        this.userPersistenceRepository = userPersistenceRepository;
        this.membershipPersistenceRepository = membershipPersistenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        UserPersistenceEntity user = userPersistenceRepository.findByEmail(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        List<TenantMembershipPersistenceEntity> memberships =
                membershipPersistenceRepository.findByUser_Id(user.getId());

        Set<GrantedAuthority> authorities = new HashSet<>();
        UUID activeTenantId = null;

        for (TenantMembershipPersistenceEntity membership : memberships) {
            if ("active".equalsIgnoreCase(membership.getStatus())) {
                if (activeTenantId == null) {
                    activeTenantId = membership.getTenant().getId();
                }
                if (membership.getAssignedRoles() != null) {
                    for (RolePersistenceEntity role : membership.getAssignedRoles()) {
                        if (role.getCode() != null) {
                            authorities.add(new SimpleGrantedAuthority(role.getCode()));
                        } else {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().replace(' ', '_').toUpperCase()));
                        }
                        if (role.getPermissions() != null) {
                            role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
                        }
                    }
                }
            }
        }

        boolean enabled = !"suspended".equalsIgnoreCase(user.getStatus());

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                activeTenantId,
                authorities,
                enabled
        );
    }
}
