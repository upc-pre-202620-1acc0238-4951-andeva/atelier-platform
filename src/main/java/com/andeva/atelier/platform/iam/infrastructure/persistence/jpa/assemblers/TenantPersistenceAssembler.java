package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bidirectional assembler converting between pure domain {@link Tenant} aggregates
 * and relational {@link TenantPersistenceEntity} instances.
 *
 * @author Joel Huamani Estefanero
 */
public final class TenantPersistenceAssembler {

    private TenantPersistenceAssembler() {
    }

    /**
     * Converts a JPA persistence entity into a pure domain {@link Tenant} aggregate root.
     *
     * @param entity the persistence entity
     * @return initialized domain Tenant, or null if entity is null
     */
    public static Tenant toDomain(TenantPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        List<Branch> branches = new ArrayList<>();
        if (entity.getBranches() != null) {
            for (BranchPersistenceEntity branchEntity : entity.getBranches()) {
                branches.add(BranchPersistenceAssembler.toDomain(branchEntity));
            }
        }

        TenantStatus status = entity.getStatus() != null
                ? TenantStatus.valueOf(entity.getStatus().trim().toUpperCase(Locale.ROOT))
                : TenantStatus.ACTIVE;

        return new Tenant(
                TenantId.of(entity.getId()),
                entity.getName(),
                entity.getLegalName(),
                TaxId.of(entity.getTaxId()),
                status,
                entity.getStripeCustomerId(),
                branches
        );
    }

    /**
     * Converts a pure domain {@link Tenant} into a JPA persistence entity.
     *
     * @param domain the domain aggregate root
     * @return initialized persistence entity, or null if domain is null
     */
    public static TenantPersistenceEntity toEntity(Tenant domain) {
        if (domain == null) {
            return null;
        }

        TenantPersistenceEntity entity = new TenantPersistenceEntity(
                domain.id().value(),
                domain.name(),
                domain.legalName(),
                domain.taxId().value(),
                domain.status().name().toLowerCase(Locale.ROOT),
                domain.stripeCustomerId()
        );

        if (domain.branches() != null) {
            for (Branch branch : domain.branches()) {
                BranchPersistenceEntity branchEntity = BranchPersistenceAssembler.toEntity(branch, entity);
                entity.addBranch(branchEntity);
            }
        }

        return entity;
    }
}
