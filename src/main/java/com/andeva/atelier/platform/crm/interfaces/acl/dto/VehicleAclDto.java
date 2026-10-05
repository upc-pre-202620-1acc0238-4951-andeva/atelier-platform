package com.andeva.atelier.platform.crm.interfaces.acl.dto;

import java.util.UUID;

/**
 * Immutable DTO exposing technical vehicle specifications and active ownership across bounded contexts.
 *
 * @param id             Universal unique identifier of the vehicle
 * @param plate          Normalized license plate
 * @param vin            Vehicle identification number (ISO 3779)
 * @param brand          Vehicle make or manufacturer
 * @param model          Commercial model name
 * @param year           Model year of manufacture
 * @param engineType     Powertrain engine classification
 * @param currentOwnerId Identifier of the currently active owner
 * @author Adiel Sanchez Santin
 */
public record VehicleAclDto(
        UUID id,
        String plate,
        String vin,
        String brand,
        String model,
        int year,
        String engineType,
        UUID currentOwnerId
) {}
