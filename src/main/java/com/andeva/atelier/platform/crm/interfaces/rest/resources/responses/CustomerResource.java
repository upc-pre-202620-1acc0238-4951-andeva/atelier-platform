package com.andeva.atelier.platform.crm.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * REST response representing a registered customer profile.
 *
 * @author Adiel Sanchez Santin
 */
public record CustomerResource(
        UUID id,
        UUID tenantId,
        String type,
        String firstName,
        String lastName,
        String companyName,
        String displayName,
        String taxId,
        String email,
        String phone,
        String status
) {}
