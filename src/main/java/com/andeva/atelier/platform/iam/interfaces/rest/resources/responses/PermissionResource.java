package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * Response projection representing an atomic platform security privilege.
 *
 * @param id          Universal unique identifier of the permission
 * @param name        Canonical permission code name (e.g., iam:tenants:read)
 * @param description Detailed technical description of the granted privilege
 * @param category    Functional module category (iam, crm, operations, inventory, hr, etc.)
 * @author Joel Huamani Estefanero
 */
public record PermissionResource(
        UUID id,
        String name,
        String description,
        String category
) {
}
