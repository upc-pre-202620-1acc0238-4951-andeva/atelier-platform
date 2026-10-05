package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record HoldTaskResource(
        @NotBlank(message = "La descripción del repuesto faltante es obligatoria")
        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String missingItemDescription,
        UUID inventoryItemId
) {}
