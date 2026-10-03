package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.entities.VerificationToken;
import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.events.EmailVerifiedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.PasswordChangedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.UserActivatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.UserRegisteredEvent;
import com.andeva.atelier.platform.iam.domain.model.events.UserSuspendedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.VerificationTokenIssuedEvent;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link User} aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("User Aggregate Root Unit Tests")
class UserAggregateTest {

    private final EmailAddress email = EmailAddress.of("carlos.mendoza@atelier.pe");
    private final Password password = Password.of("$2a$12$e8uqYVq9YwQf49tGeqr0yOXM33H7jQyH56dK50BupL2a/o9wQkX3S");
    private final PersonName name = PersonName.of("Carlos", "Mendoza");
    private final PhoneNumber phone = PhoneNumber.of("+51987654321");

    @Test
    @DisplayName("Should register local user in PENDING_VERIFICATION state and register UserRegisteredEvent")
    void shouldRegisterLocalUser() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);

        assertThat(user.id()).isNotNull();
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.password()).isEqualTo(password);
        assertThat(user.authProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(user.googleId()).isNull();
        assertThat(user.status()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.profile().getFullName()).isEqualTo("Carlos Mendoza");

        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().iterator().next()).isInstanceOf(UserRegisteredEvent.class);
    }

    @Test
    @DisplayName("Should register Google federated user in ACTIVE state and register UserRegisteredEvent")
    void shouldRegisterGoogleUser() {
        User user = User.registerWithGoogle(email, "google-sub-987654", name);

        assertThat(user.id()).isNotNull();
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.password()).isNull();
        assertThat(user.authProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(user.googleId()).isEqualTo("google-sub-987654");
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);

        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().iterator().next()).isInstanceOf(UserRegisteredEvent.class);
    }

    @Test
    @DisplayName("Should verify email and transition to ACTIVE status")
    void shouldVerifyEmail() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        user.clearDomainEvents();

        user.verifyEmail();
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);

        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().iterator().next()).isInstanceOf(EmailVerifiedEvent.class);
    }

    @Test
    @DisplayName("Should update password and register PasswordChangedEvent")
    void shouldUpdatePassword() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        user.clearDomainEvents();

        Password newPassword = Password.of("$2a$12$7kOvhE8e5iB19e3c9t4V7uGjIqJj1v8g8h6kL9a5mNb7c0vW1x2yZ");
        user.updatePassword(newPassword);

        assertThat(user.password()).isEqualTo(newPassword);
        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().iterator().next()).isInstanceOf(PasswordChangedEvent.class);
    }

    @Test
    @DisplayName("Should issue, validate, and consume verification tokens")
    void shouldIssueAndConsumeVerificationTokens() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        user.clearDomainEvents();

        VerificationToken token = user.issueVerificationToken(TokenType.EMAIL_VERIFICATION, Duration.ofMinutes(15));
        assertThat(token.tokenValue()).matches("^\\d{6}$");
        assertThat(user.verificationTokens()).hasSize(1);

        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().iterator().next()).isInstanceOf(VerificationTokenIssuedEvent.class);

        // Wrong token fails
        boolean invalidResult = user.validateAndConsumeToken("000000", TokenType.EMAIL_VERIFICATION);
        assertThat(invalidResult).isFalse();
        assertThat(token.isUsed()).isFalse();

        // Correct token succeeds and consumes
        boolean validResult = user.validateAndConsumeToken(token.tokenValue(), TokenType.EMAIL_VERIFICATION);
        assertThat(validResult).isTrue();
        assertThat(token.isUsed()).isTrue();

        // Second attempt fails because token is already consumed
        boolean reuseResult = user.validateAndConsumeToken(token.tokenValue(), TokenType.EMAIL_VERIFICATION);
        assertThat(reuseResult).isFalse();
    }

    @Test
    @DisplayName("Should manage account suspension and activation")
    void shouldManageSuspensionAndActivation() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        user.verifyEmail();
        user.clearDomainEvents();

        user.suspend();
        assertThat(user.status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().iterator().next()).isInstanceOf(UserSuspendedEvent.class);

        user.clearDomainEvents();
        user.activate();
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().iterator().next()).isInstanceOf(UserActivatedEvent.class);
    }
}
