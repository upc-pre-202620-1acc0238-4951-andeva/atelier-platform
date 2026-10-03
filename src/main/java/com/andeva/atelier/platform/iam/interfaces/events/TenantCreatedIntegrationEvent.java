package com.andeva.atelier.platform.iam.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a new tenant workshop is registered and provisioned.
 *
 * @param tenantId   Universal unique identifier of the created tenant
 * @param name       Commercial brand name of the workshop
 * @param legalName  Official legal business name registered with SUNAT
 * @param taxId      Valid 11-digit Peruvian RUC
 * @param occurredOn Timestamp of when the tenant was provisioned
 * @author Joel Huamani Estefanero
 */
public record TenantCreatedIntegrationEvent(
        UUID tenantId,
        String name,
        String legalName,
        String taxId,
        Instant occurredOn
) {
    public TenantCreatedIntegrationEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(legalName, "legalName cannot be null");
        Objects.requireNonNull(taxId, "taxId cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
