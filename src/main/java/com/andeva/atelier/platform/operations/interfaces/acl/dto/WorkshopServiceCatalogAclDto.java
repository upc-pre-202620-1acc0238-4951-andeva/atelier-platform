package com.andeva.atelier.platform.operations.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable DTO exposing workshop service catalog items across bounded contexts.
 *
 * @param id                       Service unique identifier
 * @param name                     Service commercial name
 * @param basePrice                Base labor price
 * @param estimatedDurationMinutes Estimated duration in minutes
 * @author Joel Huamani Estefanero
 */
public record WorkshopServiceCatalogAclDto(
        UUID id,
        String name,
        BigDecimal basePrice,
        int estimatedDurationMinutes
) {}
