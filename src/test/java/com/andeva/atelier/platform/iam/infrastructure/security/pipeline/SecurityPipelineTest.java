package com.andeva.atelier.platform.iam.infrastructure.security.pipeline;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantMembershipPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantMembershipPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.infrastructure.security.model.UsernamePasswordAuthenticationTokenBuilder;
import com.andeva.atelier.platform.iam.infrastructure.security.services.CustomUserDetailsService;
import com.andeva.atelier.platform.iam.infrastructure.security.tokens.BearerTokenServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Spring Security authorization pipeline components.
 *
 * @author Joel Huamani Estefanero
 */
class SecurityPipelineTest {

    @Test
    @DisplayName("CustomUserDetailsService loads user and resolves authorities and active tenant")
    void testCustomUserDetailsService() {
        UserPersistenceRepository userRepo = mock(UserPersistenceRepository.class);
        TenantMembershipPersistenceRepository memberRepo = mock(TenantMembershipPersistenceRepository.class);
        CustomUserDetailsService service = new CustomUserDetailsService(userRepo, memberRepo);

        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UserPersistenceEntity user = new UserPersistenceEntity(
                userId,
                "mechanic@atelier.pe",
                "$2a$12$hash",
                "local",
                null,
                null,
                "active"
        );
        TenantPersistenceEntity tenant = new TenantPersistenceEntity(tenantId);
        RolePersistenceEntity role = new RolePersistenceEntity(
                UUID.randomUUID(),
                tenant,
                "ROLE_MECHANIC",
                "Mechanic",
                "Patio mechanic",
                true,
                Set.of()
        );
        TenantMembershipPersistenceEntity membership = new TenantMembershipPersistenceEntity(
                UUID.randomUUID(),
                tenant,
                user,
                "active",
                "fixed",
                java.math.BigDecimal.valueOf(2500),
                Set.of(role)
        );

        when(userRepo.findByEmail("mechanic@atelier.pe")).thenReturn(Optional.of(user));
        when(memberRepo.findByUser_Id(userId)).thenReturn(List.of(membership));

        UserDetails details = service.loadUserByUsername("mechanic@atelier.pe");

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails custom = (CustomUserDetails) details;
        assertThat(custom.getUserId()).isEqualTo(userId);
        assertThat(custom.getUsername()).isEqualTo("mechanic@atelier.pe");
        assertThat(custom.getTenantId()).isEqualTo(tenantId);
        assertThat(custom.getAuthorities()).extracting("authority").contains("ROLE_MECHANIC");
        assertThat(custom.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("CustomUserDetailsService throws UsernameNotFoundException when user is not found")
    void testCustomUserDetailsServiceNotFound() {
        UserPersistenceRepository userRepo = mock(UserPersistenceRepository.class);
        TenantMembershipPersistenceRepository memberRepo = mock(TenantMembershipPersistenceRepository.class);
        CustomUserDetailsService service = new CustomUserDetailsService(userRepo, memberRepo);

        when(userRepo.findByEmail("unknown@atelier.pe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("unknown@atelier.pe"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("UsernamePasswordAuthenticationTokenBuilder constructs authentication with details")
    void testAuthenticationTokenBuilder() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Map<String, Object> claims = Map.of(
                "sub", userId.toString(),
                "email", "user@atelier.pe",
                "tenantId", tenantId.toString(),
                "permissions", List.of("mro:read", "crm:read")
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        UsernamePasswordAuthenticationToken auth =
                UsernamePasswordAuthenticationTokenBuilder.build(claims, request);

        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo("user@atelier.pe");
        assertThat(auth.getAuthorities()).hasSize(2);
    }

    @Test
    @DisplayName("UnauthorizedRequestHandlerEntryPoint renders RFC 7807 problem details")
    void testUnauthorizedEntryPoint() throws Exception {
        UnauthorizedRequestHandlerEntryPoint entryPoint =
                new UnauthorizedRequestHandlerEntryPoint(new ObjectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/work-orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authEx = mock(AuthenticationException.class);
        when(authEx.getMessage()).thenReturn("Invalid authentication token");

        entryPoint.commence(request, response, authEx);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/problem+json");
        assertThat(response.getContentAsString()).contains("https://api.atelier.pe/errors/unauthorized");
        assertThat(response.getContentAsString()).contains("Invalid authentication token");
    }

    @Test
    @DisplayName("BearerAuthorizationRequestFilter authenticates valid token and ignores missing token")
    void testBearerFilter() throws Exception {
        BearerTokenServiceImpl tokenService = mock(BearerTokenServiceImpl.class);
        BearerAuthorizationRequestFilter filter = new BearerAuthorizationRequestFilter(tokenService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenService.validateToken("valid.jwt.token")).thenReturn(true);
        when(tokenService.extractClaims("valid.jwt.token")).thenReturn(Map.of(
                "sub", UUID.randomUUID().toString(),
                "email", "test@atelier.pe"
        ));

        SecurityContextHolder.clearContext();
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(chain).doFilter(request, response);

        // Test without auth header
        SecurityContextHolder.clearContext();
        MockHttpServletRequest noAuthRequest = new MockHttpServletRequest();
        filter.doFilter(noAuthRequest, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
