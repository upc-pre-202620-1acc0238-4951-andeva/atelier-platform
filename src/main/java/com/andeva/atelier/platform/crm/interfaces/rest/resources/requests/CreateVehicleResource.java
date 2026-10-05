package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request for registering a vehicle with an initial owner.
 *
 * @author Adiel Sanchez Santin
 */
public record CreateVehicleResource(
        @NotBlank(message = "License plate is mandatory")
        @Size(max = 15, message = "Plate must not exceed 15 characters")
        String plate,

        @Size(max = 17, message = "VIN must not exceed 17 characters")
        String vin,

        @NotBlank(message = "Brand is mandatory")
        @Size(max = 50, message = "Brand must not exceed 50 characters")
        String brand,

        @NotBlank(message = "Model is mandatory")
        @Size(max = 50, message = "Model must not exceed 50 characters")
        String model,

        @jakarta.validation.constraints.Min(value = 1950, message = "Year must be greater than or equal to 1950")
        int year,

        @NotNull(message = "Engine type is mandatory")
        String engineType,

        UUID customerId,
        UUID userId
) {}
