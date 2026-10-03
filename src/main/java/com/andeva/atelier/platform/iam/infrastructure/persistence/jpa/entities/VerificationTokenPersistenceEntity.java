package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code verification_tokens} relational table.
 * Stores ephemeral OTPs and cryptographic reset tokens tied to a User account.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "verification_tokens",
        indexes = {
                @Index(name = "idx_verification_tokens_lookup", columnList = "user_id, token, type")
        }
)
public class VerificationTokenPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserPersistenceEntity user;

    @Column(name = "token", nullable = false, length = 255)
    private String token;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    public VerificationTokenPersistenceEntity(UUID id) {
        super(id);
    }

    public VerificationTokenPersistenceEntity(
            UUID id,
            UserPersistenceEntity user,
            String token,
            String type,
            Instant expiresAt,
            boolean used) {
        super(id);
        this.user = user;
        this.token = token;
        this.type = type;
        this.expiresAt = expiresAt;
        this.used = used;
    }
}
