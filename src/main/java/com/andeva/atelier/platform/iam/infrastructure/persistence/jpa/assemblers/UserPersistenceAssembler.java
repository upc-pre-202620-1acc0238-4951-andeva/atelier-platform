package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.entities.Profile;
import com.andeva.atelier.platform.iam.domain.model.entities.VerificationToken;
import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.VerificationTokenPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bidirectional assembler converting between pure domain {@link User} aggregates
 * and relational {@link UserPersistenceEntity} instances.
 *
 * @author Joel Huamani Estefanero
 */
public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {
    }

    /**
     * Converts a JPA persistence entity into a pure domain {@link User} aggregate root.
     *
     * @param entity the persistence entity
     * @return initialized domain User, or null if entity is null
     */
    public static User toDomain(UserPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        UserId userId = UserId.of(entity.getId());
        ProfilePersistenceEntity profileEntity = entity.getProfile();
        Profile profile = null;
        if (profileEntity != null) {
            PersonName personName = PersonName.of(profileEntity.getFirstName(), profileEntity.getLastName());
            PhoneNumber phoneNumber = profileEntity.getPhoneNumber() != null && !profileEntity.getPhoneNumber().isBlank()
                    ? PhoneNumber.of(profileEntity.getPhoneNumber())
                    : null;
            profile = new Profile(userId, personName, phoneNumber);
        } else {
            profile = new Profile(userId, PersonName.of("Unknown", "User"), null);
        }

        List<VerificationToken> tokens = new ArrayList<>();
        if (entity.getVerificationTokens() != null) {
            for (VerificationTokenPersistenceEntity tokenEntity : entity.getVerificationTokens()) {
                TokenType tokenType = tokenEntity.getType() != null
                        ? TokenType.valueOf(tokenEntity.getType().trim().toUpperCase(Locale.ROOT))
                        : TokenType.EMAIL_VERIFICATION;
                tokens.add(new VerificationToken(
                        tokenEntity.getId(),
                        userId,
                        tokenEntity.getToken(),
                        tokenType,
                        tokenEntity.getExpiresAt(),
                        tokenEntity.isUsed()
                ));
            }
        }

        AuthProvider authProvider = entity.getAuthProvider() != null
                ? AuthProvider.valueOf(entity.getAuthProvider().trim().toUpperCase(Locale.ROOT))
                : AuthProvider.LOCAL;

        UserStatus userStatus = entity.getStatus() != null
                ? UserStatus.valueOf(entity.getStatus().trim().toUpperCase(Locale.ROOT))
                : UserStatus.PENDING_VERIFICATION;

        Password password = entity.getPasswordHash() != null && !entity.getPasswordHash().isBlank()
                ? Password.fromHash(entity.getPasswordHash())
                : null;

        return new User(
                userId,
                EmailAddress.of(entity.getEmail()),
                password,
                authProvider,
                entity.getGoogleId(),
                entity.getFcmToken(),
                userStatus,
                profile,
                tokens
        );
    }

    /**
     * Converts a pure domain {@link User} into a JPA persistence entity.
     *
     * @param domain the domain aggregate root
     * @return initialized persistence entity, or null if domain is null
     */
    public static UserPersistenceEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserPersistenceEntity entity = new UserPersistenceEntity(
                domain.id().value(),
                domain.email().value(),
                domain.password() != null ? domain.password().hashedValue() : null,
                domain.authProvider().name().toLowerCase(Locale.ROOT),
                domain.googleId(),
                domain.fcmToken(),
                domain.status().name().toLowerCase(Locale.ROOT)
        );

        if (domain.profile() != null) {
            ProfilePersistenceEntity profileEntity = new ProfilePersistenceEntity(
                    entity,
                    domain.profile().name().firstName(),
                    domain.profile().name().lastName(),
                    domain.profile().phone() != null ? domain.profile().phone().value() : null,
                    null
            );
            entity.setProfile(profileEntity);
        }

        if (domain.verificationTokens() != null) {
            for (VerificationToken token : domain.verificationTokens()) {
                VerificationTokenPersistenceEntity tokenEntity = new VerificationTokenPersistenceEntity(
                        token.id(),
                        entity,
                        token.tokenValue(),
                        token.type().name().toLowerCase(Locale.ROOT),
                        token.expiresAt(),
                        token.isUsed()
                );
                entity.addVerificationToken(tokenEntity);
            }
        }

        return entity;
    }
}
