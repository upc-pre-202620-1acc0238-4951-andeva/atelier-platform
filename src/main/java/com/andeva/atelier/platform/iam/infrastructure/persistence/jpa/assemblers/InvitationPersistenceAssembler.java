package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Locale;

/**
 * Bidirectional assembler converting between pure domain {@link Invitation} aggregates
 * and relational {@link InvitationPersistenceEntity} instances.
 *
 * @author Joel Huamani Estefanero
 */
public final class InvitationPersistenceAssembler {

    private InvitationPersistenceAssembler() {
    }

    /**
     * Converts a JPA persistence entity into a pure domain {@link Invitation} aggregate root.
     *
     * @param entity the persistence entity
     * @return initialized domain Invitation, or null if entity is null
     */
    public static Invitation toDomain(InvitationPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        TenantId tenantId = entity.getTenant() != null
                ? TenantId.of(entity.getTenant().getId())
                : null;

        InvitationStatus status = entity.getStatus() != null
                ? InvitationStatus.valueOf(entity.getStatus().trim().toUpperCase(Locale.ROOT))
                : InvitationStatus.PENDING;

        return new Invitation(
                InvitationId.of(entity.getId()),
                tenantId,
                EmailAddress.of(entity.getEmail()),
                entity.getToken(),
                status,
                RoleId.of(entity.getTargetRoleId()),
                entity.getExpiresAt()
        );
    }

    /**
     * Converts a pure domain {@link Invitation} into a JPA persistence entity.
     *
     * @param domain the domain aggregate root
     * @param tenant the parent tenant persistence entity
     * @return initialized persistence entity, or null if domain is null
     */
    public static InvitationPersistenceEntity toEntity(Invitation domain, TenantPersistenceEntity tenant) {
        if (domain == null) {
            return null;
        }

        return new InvitationPersistenceEntity(
                domain.id().value(),
                tenant,
                domain.email().value(),
                domain.targetRoleId().value(),
                domain.token(),
                domain.status().name().toLowerCase(Locale.ROOT),
                domain.expiresAt()
        );
    }
}
