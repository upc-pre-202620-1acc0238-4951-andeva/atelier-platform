package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantMembershipPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Bidirectional assembler converting between pure domain {@link TenantMembership} aggregates
 * and relational {@link TenantMembershipPersistenceEntity} instances.
 *
 * @author Joel Huamani Estefanero
 */
public final class TenantMembershipPersistenceAssembler {

    private TenantMembershipPersistenceAssembler() {
    }

    /**
     * Converts a JPA persistence entity into a pure domain {@link TenantMembership} aggregate root.
     *
     * @param entity the persistence entity
     * @return initialized domain TenantMembership, or null if entity is null
     */
    public static TenantMembership toDomain(TenantMembershipPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        Set<Role> assignedRoles = new HashSet<>();
        if (entity.getAssignedRoles() != null) {
            for (RolePersistenceEntity roleEntity : entity.getAssignedRoles()) {
                assignedRoles.add(RolePersistenceAssembler.toDomain(roleEntity));
            }
        }

        TenantId tenantId = entity.getTenant() != null
                ? TenantId.of(entity.getTenant().getId())
                : null;
        UserId userId = entity.getUser() != null
                ? UserId.of(entity.getUser().getId())
                : null;

        MembershipStatus status = entity.getStatus() != null
                ? MembershipStatus.valueOf(entity.getStatus().trim().toUpperCase(Locale.ROOT))
                : MembershipStatus.ACTIVE;

        SalaryType salaryType = entity.getSalaryType() != null
                ? SalaryType.valueOf(entity.getSalaryType().trim().toUpperCase(Locale.ROOT))
                : SalaryType.FIXED;

        Money baseSalary = entity.getBaseSalary() != null
                ? Money.soles(entity.getBaseSalary())
                : Money.ZERO_PEN;

        return new TenantMembership(
                TenantMembershipId.of(entity.getId()),
                tenantId,
                userId,
                status,
                salaryType,
                baseSalary,
                assignedRoles
        );
    }

    /**
     * Converts a pure domain {@link TenantMembership} into a JPA persistence entity.
     *
     * @param domain the domain aggregate root
     * @param tenant the parent tenant persistence entity
     * @param user the linked user persistence entity
     * @param assignedRoles set of resolved role persistence entities
     * @return initialized persistence entity, or null if domain is null
     */
    public static TenantMembershipPersistenceEntity toEntity(
            TenantMembership domain,
            TenantPersistenceEntity tenant,
            UserPersistenceEntity user,
            Set<RolePersistenceEntity> assignedRoles) {
        if (domain == null) {
            return null;
        }

        return new TenantMembershipPersistenceEntity(
                domain.id().value(),
                tenant,
                user,
                domain.status().name().toLowerCase(Locale.ROOT),
                domain.salaryType().name().toLowerCase(Locale.ROOT),
                domain.baseSalary() != null ? domain.baseSalary().amount() : null,
                assignedRoles != null ? assignedRoles : new HashSet<>()
        );
    }
}
