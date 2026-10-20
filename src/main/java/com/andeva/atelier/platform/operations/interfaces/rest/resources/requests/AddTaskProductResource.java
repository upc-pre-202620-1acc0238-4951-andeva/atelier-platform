package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record AddTaskProductResource(
        @NotNull(message = "El identificador del producto es obligatorio")
        UUID productId,
        @NotNull(message = "La cantidad es mandatoria")
        @Positive(message = "La cantidad demandada debe ser estrictamente positiva")
        BigDecimal quantity,
        // Optional: when omitted the catalog base price of the part is applied
        @Positive(message = "El precio unitario debe ser positivo")
        BigDecimal unitPrice,
        @Pattern(regexp = "PEN|USD", message = "La divisa debe ser PEN o USD")
        String currency
) {}
