package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST request for transferring vehicle legal ownership to a new customer.
 *
 * @author Adiel Sanchez Santin
 */
public record TransferVehicleOwnershipResource(
        @NotNull(message = "New owner ID is mandatory")
        UUID newOwnerId,

        @NotNull(message = "Transfer date is mandatory")
        LocalDate transferDate
) {}
