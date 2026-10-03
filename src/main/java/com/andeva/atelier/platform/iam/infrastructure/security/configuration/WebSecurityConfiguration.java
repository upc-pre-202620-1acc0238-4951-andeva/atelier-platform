package com.andeva.atelier.platform.iam.infrastructure.security.configuration;

import com.andeva.atelier.platform.iam.infrastructure.security.pipeline.BearerAuthorizationRequestFilter;
import com.andeva.atelier.platform.iam.infrastructure.security.pipeline.UnauthorizedRequestHandlerEntryPoint;
import com.andeva.atelier.platform.iam.infrastructure.security.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6 configuration establishing the perimeter security filter chain,
 * stateless session policy, CORS rules, and Bearer JWT token verification.
 *
 * @author Joel Huamani Estefanero
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private final BearerAuthorizationRequestFilter bearerAuthorizationRequestFilter;
    private final UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandlerEntryPoint;
    private final CustomUserDetailsService customUserDetailsService;
    private final List<String> allowedOrigins;

    public WebSecurityConfiguration(
            BearerAuthorizationRequestFilter bearerAuthorizationRequestFilter,
            UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandlerEntryPoint,
            CustomUserDetailsService customUserDetailsService,
            @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173,http://localhost:8080}") String allowedOrigins) {
        this.bearerAuthorizationRequestFilter = bearerAuthorizationRequestFilter;
        this.unauthorizedRequestHandlerEntryPoint = unauthorizedRequestHandlerEntryPoint;
        this.customUserDetailsService = customUserDetailsService;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(unauthorizedRequestHandlerEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/authentication/**",
                                "/api/v1/authentication/**",
                                "/auth/**",
                                "/api/v1/auth/**",
                                "/invitations/validate",
                                "/api/v1/invitations/validate",
                                "/invitations/accept",
                                "/api/v1/invitations/accept",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(bearerAuthorizationRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
