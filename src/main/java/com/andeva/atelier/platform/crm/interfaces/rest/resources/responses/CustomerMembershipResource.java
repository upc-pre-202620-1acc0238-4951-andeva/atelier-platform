package com.andeva.atelier.platform.crm.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * REST response representing a corporate customer fleet membership.
 *
 * @author Adiel Sanchez Santin
 */
public record CustomerMembershipResource(
        UUID id,
        UUID customerId,
        UUID userId,
        String role,
        String status
) {}
