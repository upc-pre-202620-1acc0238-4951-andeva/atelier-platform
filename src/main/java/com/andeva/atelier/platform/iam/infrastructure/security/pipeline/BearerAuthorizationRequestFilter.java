package com.andeva.atelier.platform.iam.infrastructure.security.pipeline;

import com.andeva.atelier.platform.iam.infrastructure.security.model.UsernamePasswordAuthenticationTokenBuilder;
import com.andeva.atelier.platform.iam.infrastructure.security.tokens.BearerTokenServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Perimeter security filter intercepting Bearer JWT tokens in HTTP requests,
 * validating HMAC-SHA256 signatures, and establishing the Spring Security context.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final BearerTokenServiceImpl bearerTokenService;

    public BearerAuthorizationRequestFilter(BearerTokenServiceImpl bearerTokenService) {
        this.bearerTokenService = bearerTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            if (bearerTokenService.validateToken(token)) {
                Map<String, Object> claims = bearerTokenService.extractClaims(token);
                if (!claims.isEmpty() && claims.containsKey("sub")) {
                    UsernamePasswordAuthenticationToken authentication =
                            UsernamePasswordAuthenticationTokenBuilder.build(claims, request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
