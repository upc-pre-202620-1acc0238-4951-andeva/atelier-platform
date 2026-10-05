package com.andeva.atelier.platform.crm.interfaces.acl.dto;

import java.util.UUID;

/**
 * Immutable DTO exposing corporate fleet membership and user roles across bounded contexts.
 *
 * @param id         Unique identifier of the membership
 * @param customerId Identifier of the corporate fleet customer
 * @param userId     Identifier of the delegated user
 * @param role       Role in the fleet (FLEET_ADMIN or FLEET_OPERATOR)
 * @param status     Operational status (ACTIVE, SUSPENDED, REVOKED)
 * @author Adiel Sanchez Santin
 */
public record CustomerMembershipAclDto(
        UUID id,
        UUID customerId,
        UUID userId,
        String role,
        String status
) {}
