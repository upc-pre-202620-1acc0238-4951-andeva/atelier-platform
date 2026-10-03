package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * Compact summary projection of a tenant workshop for nested security context and tokens.
 *
 * @param id    Universal identifier of the workshop tenant
 * @param name  Commercial brand name of the workshop
 * @param taxId 11-digit SUNAT RUC
 * @author Joel Huamani Estefanero
 */
public record TenantSummaryResource(
        UUID id,
        String name,
        String taxId
) {
}
