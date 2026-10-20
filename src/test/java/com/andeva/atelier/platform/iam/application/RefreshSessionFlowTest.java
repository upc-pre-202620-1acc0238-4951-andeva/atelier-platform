package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.internal.commandservices.RefreshSessionManager;
import com.andeva.atelier.platform.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.GoogleIdentityGateway;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BearerTokenService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.RefreshSession;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RefreshAccessTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.SignOutCommand;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.RefreshSessionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Refresh token rotation and sign-out (TS01, US04)")
class RefreshSessionFlowTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;
    @Mock
    private BCryptHashingService hashingService;
    @Mock
    private BearerTokenService bearerTokenService;
    @Mock
    private GoogleIdentityGateway googleIdentityGateway;

    private final Map<String, RefreshSession> sessions = new HashMap<>();
    private UserCommandServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        RefreshSessionRepository repository = new RefreshSessionRepository() {
            @Override
            public RefreshSession save(RefreshSession session) {
                sessions.put(session.tokenHash(), session);
                return session;
            }

            @Override
            public Optional<RefreshSession> findByTokenHash(String tokenHash) {
                return Optional.ofNullable(sessions.get(tokenHash));
            }
        };
        service = new UserCommandServiceImpl(userRepository, membershipRepository, hashingService,
                bearerTokenService, googleIdentityGateway, new RefreshSessionManager(repository));

        EmailAddress email = EmailAddress.of("owner@atelier.pe");
        user = User.registerWithLocalCredentials(email, Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012"),
                PersonName.of("Ana", "Rojas"), null);
        user.activate();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hashingService.matches(any(), any())).thenReturn(true);
        when(membershipRepository.findByUserId(user.id())).thenReturn(List.of());
        when(bearerTokenService.generateToken(any(), any(), any())).thenReturn("access.jwt");
    }

    @Test
    @DisplayName("Sign-in returns a refresh token that renews once and is then spent")
    void shouldRotateRefreshToken() {
        AuthenticatedUser signedIn = service.handle(new AuthenticateUserCommand(EmailAddress.of("owner@atelier.pe"), "Secret#2026")).getOrThrow();
        assertThat(signedIn.refreshToken()).isNotBlank();
        assertThat(sessions.keySet()).doesNotContain(signedIn.refreshToken());
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

        Result<AuthenticatedUser, ApplicationError> renewed = service.handle(new RefreshAccessTokenCommand(signedIn.refreshToken()));
        assertThat(renewed.isSuccess()).isTrue();
        assertThat(renewed.getOrThrow().refreshToken()).isNotEqualTo(signedIn.refreshToken());

        Result<AuthenticatedUser, ApplicationError> replayed = service.handle(new RefreshAccessTokenCommand(signedIn.refreshToken()));
        assertThat(replayed.isFailure()).isTrue();
        assertThat(replayed.getError().code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("Sign-out revokes the refresh token")
    void shouldRevokeOnSignOut() {
        AuthenticatedUser signedIn = service.handle(new AuthenticateUserCommand(EmailAddress.of("owner@atelier.pe"), "Secret#2026")).getOrThrow();

        assertThat(service.handle(new SignOutCommand(signedIn.refreshToken())).isSuccess()).isTrue();

        assertThat(service.handle(new RefreshAccessTokenCommand(signedIn.refreshToken())).isFailure()).isTrue();
    }
}
