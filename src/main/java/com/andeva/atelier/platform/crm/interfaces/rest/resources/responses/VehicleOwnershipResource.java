package com.andeva.atelier.platform.crm.interfaces.rest.resources.responses;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST response representing a vehicle ownership record in custody history.
 *
 * @author Adiel Sanchez Santin
 */
public record VehicleOwnershipResource(
        UUID id,
        UUID vehicleId,
        UUID customerId,
        UUID userId,
        LocalDate startDate,
        LocalDate endDate
) {}
