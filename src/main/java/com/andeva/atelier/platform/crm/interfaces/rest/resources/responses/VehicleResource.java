package com.andeva.atelier.platform.crm.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * REST response representing a universal vehicle.
 *
 * @author Adiel Sanchez Santin
 */
public record VehicleResource(
        UUID id,
        String plate,
        String vin,
        String brand,
        String model,
        int year,
        String engineType,
        UUID currentOwnerId
) {}
