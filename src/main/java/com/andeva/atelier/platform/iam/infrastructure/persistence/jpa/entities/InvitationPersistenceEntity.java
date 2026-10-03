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
 * JPA persistence entity mapped to the {@code invitations} relational table.
 * Governs staff onboarding invitations and cryptographic token redemption.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "invitations",
        indexes = {
                @Index(name = "idx_invitations_tenant_status", columnList = "tenant_id, status"),
                @Index(name = "idx_invitations_token", columnList = "token", unique = true)
        }
)
public class InvitationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantPersistenceEntity tenant;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "target_role_id", nullable = false)
    private UUID targetRoleId;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public InvitationPersistenceEntity(UUID id) {
        super(id);
    }

    public InvitationPersistenceEntity(
            UUID id,
            TenantPersistenceEntity tenant,
            String email,
            UUID targetRoleId,
            String token,
            String status,
            Instant expiresAt) {
        super(id);
        this.tenant = tenant;
        this.email = email;
        this.targetRoleId = targetRoleId;
        this.token = token;
        this.status = status;
        this.expiresAt = expiresAt;
    }
}
