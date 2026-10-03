package com.andeva.atelier.platform.iam.infrastructure.security.model;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory builder for assembling authenticated {@link UsernamePasswordAuthenticationToken} instances
 * from JWT claims payloads and HTTP servlet request context.
 *
 * @author Joel Huamani Estefanero
 */
public final class UsernamePasswordAuthenticationTokenBuilder {

    private UsernamePasswordAuthenticationTokenBuilder() {
    }

    /**
     * Builds an authentication token from decoded JWT claims.
     *
     * @param claims decoded JWT claims map
     * @param request current HTTP request
     * @return fully populated authentication token
     */
    public static UsernamePasswordAuthenticationToken build(Map<String, Object> claims, HttpServletRequest request) {
        String userIdStr = (String) claims.get("sub");
        String email = (String) claims.get("email");
        String tenantIdStr = (String) claims.get("tenantId");

        UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;
        UUID tenantId = tenantIdStr != null ? UUID.fromString(tenantIdStr) : null;

        List<GrantedAuthority> authorities = new ArrayList<>();
        Object permsObj = claims.get("permissions");
        if (permsObj instanceof Collection<?> perms) {
            for (Object perm : perms) {
                if (perm != null) {
                    authorities.add(new SimpleGrantedAuthority(perm.toString()));
                }
            }
        }

        CustomUserDetails userDetails = new CustomUserDetails(
                userId != null ? userId : UUID.randomUUID(),
                email != null ? email : "anonymous",
                null,
                tenantId,
                authorities,
                true
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        if (request != null) {
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }

        return authentication;
    }
}
