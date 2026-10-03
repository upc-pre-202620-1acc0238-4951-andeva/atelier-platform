package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.TenantResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.TenantSummaryResource;

import java.time.Instant;
import java.util.Objects;

/**
 * Assembler projecting domain {@link Tenant} aggregates into REST {@link TenantResource}
 * and {@link TenantSummaryResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
public final class TenantResourceFromAggregateAssembler {

    private TenantResourceFromAggregateAssembler() {
    }

    /**
     * Converts a {@link Tenant} aggregate root into a full {@link TenantResource}.
     *
     * @param tenant Domain aggregate root
     * @return REST response projection
     */
    public static TenantResource toResourceFromAggregate(Tenant tenant) {
        Objects.requireNonNull(tenant, "Tenant aggregate cannot be null");
        return new TenantResource(
                tenant.id().value(),
                tenant.name(),
                tenant.legalName(),
                tenant.taxId().value(),
                tenant.status().name(),
                tenant.stripeCustomerId(),
                Instant.now()
        );
    }

    /**
     * Converts a {@link Tenant} aggregate root into a compact {@link TenantSummaryResource}.
     *
     * @param tenant Domain aggregate root
     * @return Compact REST response projection
     */
    public static TenantSummaryResource toSummaryFromAggregate(Tenant tenant) {
        Objects.requireNonNull(tenant, "Tenant aggregate cannot be null");
        return new TenantSummaryResource(
                tenant.id().value(),
                tenant.name(),
                tenant.taxId().value()
        );
    }
}
