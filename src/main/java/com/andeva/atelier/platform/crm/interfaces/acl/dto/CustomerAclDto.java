package com.andeva.atelier.platform.crm.interfaces.acl.dto;

import java.util.UUID;

/**
 * Immutable DTO exposing customer fiscal and contact information across bounded contexts.
 *
 * @param id          Unique identifier of the customer
 * @param tenantId    Identifier of the workshop tenant
 * @param type        Classification type (INDIVIDUAL or COMPANY)
 * @param displayName Concatenated full name or legal company name
 * @param taxId       Fiscal tax identifier (DNI or RUC)
 * @param email       Primary email address
 * @param phone       Primary contact phone number
 * @param status      Account status (ACTIVE or INACTIVE)
 * @author Adiel Sanchez Santin
 */
public record CustomerAclDto(
        UUID id,
        UUID tenantId,
        String type,
        String displayName,
        String taxId,
        String email,
        String phone,
        String status
) {}
