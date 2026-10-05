package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateTaskProductResource(
        @NotNull(message = "La cantidad demandada es obligatoria")
        @Positive(message = "La cantidad demandada debe ser estrictamente positiva")
        BigDecimal quantity
) {}
