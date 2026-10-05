package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateWorkOrderTaskResource(
        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        String description,
        @Positive(message = "El precio de mano de obra debe ser positivo")
        BigDecimal price,
        UUID mechanicId
) {}
