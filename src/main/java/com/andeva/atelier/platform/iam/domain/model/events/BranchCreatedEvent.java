package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a new physical workshop branch is added to a Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record BranchCreatedEvent(
        BranchId branchId,
        TenantId tenantId,
        String name,
        Instant occurredOn
) implements Serializable {

    public BranchCreatedEvent {
        Objects.requireNonNull(branchId, "Branch identifier cannot be null");
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(name, "Branch name cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static BranchCreatedEvent of(BranchId branchId, TenantId tenantId, String name) {
        return new BranchCreatedEvent(branchId, tenantId, name, Instant.now());
    }
}
