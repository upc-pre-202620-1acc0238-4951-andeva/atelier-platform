package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code users} relational table.
 * Represents the universal identity and authentication credentials across the platform.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true),
                @Index(name = "idx_users_google_id", columnList = "google_id")
        }
)
public class UserPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "auth_provider", nullable = false, length = 20)
    private String authProvider;

    @Column(name = "google_id", length = 255)
    private String googleId;

    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfilePersistenceEntity profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationTokenPersistenceEntity> verificationTokens = new ArrayList<>();

    public UserPersistenceEntity(UUID id) {
        super(id);
    }

    public UserPersistenceEntity(
            UUID id,
            String email,
            String passwordHash,
            String authProvider,
            String googleId,
            String fcmToken,
            String status) {
        super(id);
        this.email = email;
        this.passwordHash = passwordHash;
        this.authProvider = authProvider;
        this.googleId = googleId;
        this.fcmToken = fcmToken;
        this.status = status;
        this.verificationTokens = new ArrayList<>();
    }

    /**
     * Helper to establish bidirectional 1:1 relationship with profile.
     */
    public void setProfile(ProfilePersistenceEntity profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setUser(this);
        }
    }

    /**
     * Helper to add a verification token to this user.
     */
    public void addVerificationToken(VerificationTokenPersistenceEntity token) {
        this.verificationTokens.add(token);
        token.setUser(this);
    }

    /**
     * Helper to remove a verification token from this user.
     */
    public void removeVerificationToken(VerificationTokenPersistenceEntity token) {
        this.verificationTokens.remove(token);
        token.setUser(null);
    }
}
