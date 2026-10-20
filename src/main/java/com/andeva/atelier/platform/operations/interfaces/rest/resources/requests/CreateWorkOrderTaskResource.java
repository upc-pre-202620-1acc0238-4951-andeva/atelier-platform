package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWorkOrderTaskResource(
        @NotNull(message = "El identificador del servicio tarifario es obligatorio")
        UUID serviceId,
        UUID mechanicId,
        @NotBlank(message = "La descripción de la tarea es obligatoria")
        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        String description,
        // Optional: when omitted the catalog base price of the service is applied
        @Positive(message = "El precio de mano de obra debe ser estrictamente positivo")
        BigDecimal price,
        @Pattern(regexp = "PEN|USD", message = "La divisa debe ser PEN o USD")
        String currency
) {}
