package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateWorkOrderResource(
        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        Integer mileageIn,
        @Size(max = 2000, message = "El diagnóstico no puede superar 2000 caracteres")
        String diagnosticSummary
) {}
