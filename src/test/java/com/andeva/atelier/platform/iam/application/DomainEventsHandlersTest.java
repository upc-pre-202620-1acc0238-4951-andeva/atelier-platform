package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.internal.eventhandlers.TenantDomainEventsHandler;
import com.andeva.atelier.platform.iam.application.internal.eventhandlers.UserDomainEventsHandler;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.ResendEmailService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.events.PasswordResetRequestedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.StaffInvitedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantRegisteredEvent;
import com.andeva.atelier.platform.iam.domain.model.events.VerificationTokenIssuedEvent;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit test suite for IAM Application Domain Event Handlers.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IAM Domain Event Handlers Unit Tests")
class DomainEventsHandlersTest {

    @Mock
    private ResendEmailService resendEmailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantRepository tenantRepository;

    @Test
    @DisplayName("UserDomainEventsHandler: should send verification email on email verification token issued")
    void shouldSendVerificationEmailOnTokenIssued() {
        UserDomainEventsHandler handler = new UserDomainEventsHandler(resendEmailService, userRepository);

        UserId userId = UserId.generate();
        EmailAddress email = EmailAddress.of("user@atelier.pe");
        User user = User.registerWithLocalCredentials(
                email,
                Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012"),
                PersonName.of("Carlos", "García"),
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        VerificationTokenIssuedEvent event = VerificationTokenIssuedEvent.of(
                userId, "123456", TokenType.EMAIL_VERIFICATION, Instant.now().plusSeconds(900)
        );

        handler.on(event);

        verify(resendEmailService).sendVerificationEmail(email, "123456");
    }

    @Test
    @DisplayName("UserDomainEventsHandler: should send password reset email on reset requested")
    void shouldSendPasswordResetEmail() {
        UserDomainEventsHandler handler = new UserDomainEventsHandler(resendEmailService, userRepository);

        EmailAddress email = EmailAddress.of("user@atelier.pe");
        PasswordResetRequestedEvent event = PasswordResetRequestedEvent.of(
                UserId.generate(), email, "reset-token-xyz", Instant.now().plusSeconds(7200)
        );

        handler.on(event);

        verify(resendEmailService).sendPasswordResetEmail(email, "reset-token-xyz");
    }

    @Test
    @DisplayName("TenantDomainEventsHandler: should send staff invitation email on staff invited")
    void shouldSendStaffInvitationEmail() {
        TenantDomainEventsHandler handler = new TenantDomainEventsHandler(resendEmailService, tenantRepository);

        TenantId tenantId = TenantId.generate();
        Tenant tenant = Tenant.create("AutoTaller Pro", "AutoTaller S.A.", TaxId.ruc("20456789014"));
        EmailAddress email = EmailAddress.of("mechanic@atelier.pe");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        StaffInvitedEvent event = StaffInvitedEvent.of(InvitationId.generate(), tenantId, email, "invite-token-abc");

        handler.on(event);

        verify(resendEmailService).sendStaffInvitationEmail(email, "AutoTaller Pro", "invite-token-abc");
    }

    @Test
    @DisplayName("TenantDomainEventsHandler: should handle TenantRegisteredEvent after commit")
    void shouldHandleTenantRegisteredEvent() {
        TenantDomainEventsHandler handler = new TenantDomainEventsHandler(resendEmailService, tenantRepository);

        TenantRegisteredEvent event = TenantRegisteredEvent.of(
                TenantId.generate(), "AutoTaller Pro", TaxId.ruc("20456789014")
        );

        handler.on(event);
        // Asserts execution without errors or exceptions
    }
}
