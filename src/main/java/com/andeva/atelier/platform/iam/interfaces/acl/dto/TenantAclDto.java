package com.andeva.atelier.platform.iam.interfaces.acl.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Inbound Anti-Corruption Layer DTO representing an automotive workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantAclDto(
        UUID tenantId,
        String name,
        String legalName,
        String taxId,
        String status
) implements Serializable {
}
