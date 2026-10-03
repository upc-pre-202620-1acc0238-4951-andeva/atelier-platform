package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.entities.Profile;
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
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Sovereign authentication and universal user identity aggregate root across the Atelier Platform.
 *
 * @author Joel Huamani Estefanero
 */
public class User extends AbstractDomainAggregateRoot<User> {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserId id;
    private final EmailAddress email;
    private Password password;
    private final AuthProvider authProvider;
    private String googleId;
    private String fcmToken;
    private UserStatus status;
    private Profile profile;
    private final List<VerificationToken> verificationTokens;

    public User(
            UserId id,
            EmailAddress email,
            Password password,
            AuthProvider authProvider,
            String googleId,
            String fcmToken,
            UserStatus status,
            Profile profile,
            List<VerificationToken> verificationTokens) {
        this.id = Objects.requireNonNull(id, "User identifier cannot be null");
        this.email = Objects.requireNonNull(email, "Email address cannot be null");
        this.authProvider = Objects.requireNonNull(authProvider, "Auth provider cannot be null");
        this.status = Objects.requireNonNull(status, "User status cannot be null");
        this.profile = Objects.requireNonNull(profile, "Profile cannot be null");
        this.fcmToken = fcmToken;
        this.verificationTokens = verificationTokens != null ? new ArrayList<>(verificationTokens) : new ArrayList<>();

        if (authProvider == AuthProvider.LOCAL) {
            this.password = Objects.requireNonNull(password, "Local credentials require a password hash");
            this.googleId = null;
        } else {
            this.googleId = Objects.requireNonNull(googleId, "Google provider requires a googleId");
            this.password = null;
        }
    }

    /**
     * Domain factory to register a user using local email and BCrypt password credentials.
     * Starts in PENDING_VERIFICATION until email verification OTP is completed.
     */
    public static User registerWithLocalCredentials(
            EmailAddress email,
            Password password,
            PersonName name,
            PhoneNumber phone) {
        UserId userId = UserId.generate();
        Profile userProfile = Profile.create(userId, name, phone);
        User user = new User(
                userId,
                email,
                password,
                AuthProvider.LOCAL,
                null,
                null,
                UserStatus.PENDING_VERIFICATION,
                userProfile,
                new ArrayList<>()
        );
        user.registerEvent(UserRegisteredEvent.of(userId, email));
        return user;
    }

    /**
     * Domain factory to register a federated user via Google OAuth2 SSO.
     * Google-verified emails transition immediately to ACTIVE.
     */
    public static User registerWithGoogle(
            EmailAddress email,
            String googleId,
            PersonName name) {
        UserId userId = UserId.generate();
        Profile userProfile = Profile.create(userId, name, null);
        User user = new User(
                userId,
                email,
                null,
                AuthProvider.GOOGLE,
                googleId,
                null,
                UserStatus.ACTIVE,
                userProfile,
                new ArrayList<>()
        );
        user.registerEvent(UserRegisteredEvent.of(userId, email));
        return user;
    }

    /**
     * Verifies the email address and activates the user account.
     */
    public void verifyEmail() {
        this.status = UserStatus.ACTIVE;
        registerEvent(EmailVerifiedEvent.of(this.id));
    }

    /**
     * Updates the password hash and registers a PasswordChangedEvent.
     */
    public void updatePassword(Password newPassword) {
        this.password = Objects.requireNonNull(newPassword, "New password cannot be null");
        registerEvent(PasswordChangedEvent.of(this.id));
    }

    /**
     * Updates the Firebase Cloud Messaging device token.
     */
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Updates demographic profile details of the user.
     */
    public void updateProfile(com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName name,
                              com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber phone) {
        if (this.profile == null) {
            this.profile = Profile.create(this.id, name, phone);
        } else {
            this.profile.update(name, phone);
        }
    }

    /**
     * Issues a one-time verification token (6-digit numeric OTP for email verification or UUID for password reset).
     */
    public VerificationToken issueVerificationToken(TokenType type, Duration validity) {
        Objects.requireNonNull(type, "Token type cannot be null");
        Objects.requireNonNull(validity, "Validity duration cannot be null");

        String tokenValue;
        if (type == TokenType.EMAIL_VERIFICATION || type == TokenType.LOGIN_OTP) {
            tokenValue = "%06d".formatted(SECURE_RANDOM.nextInt(1_000_000));
        } else {
            tokenValue = UUID.randomUUID().toString().replace("-", "");
        }

        Instant expiresAt = Instant.now().plus(validity);
        VerificationToken token = VerificationToken.issue(this.id, tokenValue, type, expiresAt);
        this.verificationTokens.add(token);
        registerEvent(VerificationTokenIssuedEvent.of(this.id, tokenValue, type, expiresAt));
        return token;
    }

    /**
     * Validates and marks as consumed a matching verification token.
     */
    public boolean validateAndConsumeToken(String tokenValue, TokenType type) {
        Objects.requireNonNull(tokenValue, "Token value cannot be null");
        Objects.requireNonNull(type, "Token type cannot be null");

        for (VerificationToken token : this.verificationTokens) {
            if (token.type() == type && token.isValid() && token.tokenValue().equals(tokenValue.trim())) {
                token.consume();
                return true;
            }
        }
        return false;
    }

    /**
     * Suspends the user account.
     */
    public void suspend() {
        if (this.status == UserStatus.SUSPENDED) {
            return;
        }
        this.status = UserStatus.SUSPENDED;
        registerEvent(UserSuspendedEvent.of(this.id));
    }

    /**
     * Activates the user account.
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            return;
        }
        this.status = UserStatus.ACTIVE;
        registerEvent(UserActivatedEvent.of(this.id));
    }

    public UserId id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public Password password() {
        return password;
    }

    public AuthProvider authProvider() {
        return authProvider;
    }

    public String googleId() {
        return googleId;
    }

    public String fcmToken() {
        return fcmToken;
    }

    public UserStatus status() {
        return status;
    }

    public Profile profile() {
        return profile;
    }

    public List<VerificationToken> verificationTokens() {
        return Collections.unmodifiableList(verificationTokens);
    }
}
