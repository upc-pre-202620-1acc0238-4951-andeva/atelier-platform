package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.time.Instant;
import java.util.UUID;

/**
 * Response projection representing a workshop tenant corporate entity.
 *
 * @param id               Universal unique identifier of the workshop tenant
 * @param name             Commercial or brand trade name
 * @param legalName        Official registered business legal name
 * @param taxId            Valid 11-digit SUNAT RUC
 * @param status           Operational status (ACTIVE, PENDING, SUSPENDED)
 * @param stripeCustomerId Stripe customer ID for subscription billing (or null)
 * @param createdAt        Timestamp of tenant registration in UTC
 * @author Joel Huamani Estefanero
 */
public record TenantResource(
        UUID id,
        String name,
        String legalName,
        String taxId,
        String status,
        String stripeCustomerId,
        Instant createdAt
) {
}
