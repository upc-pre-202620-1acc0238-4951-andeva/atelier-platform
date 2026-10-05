package com.andeva.atelier.platform.crm.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a new customer is registered.
 *
 * @param customerId  Unique identifier of the customer
 * @param tenantId    Identifier of the workshop tenant
 * @param type        Classification type (INDIVIDUAL or COMPANY)
 * @param displayName Business name or person full name
 * @param taxId       Fiscal tax identifier (DNI or RUC)
 * @param email       Email address
 * @param phone       Phone number
 * @param occurredOn  Timestamp when the event occurred
 * @author Adiel Sanchez Santin
 */
public record CustomerCreatedIntegrationEvent(
        UUID customerId,
        UUID tenantId,
        String type,
        String displayName,
        String taxId,
        String email,
        String phone,
        Instant occurredOn
) {
    public CustomerCreatedIntegrationEvent {
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(taxId, "taxId cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
