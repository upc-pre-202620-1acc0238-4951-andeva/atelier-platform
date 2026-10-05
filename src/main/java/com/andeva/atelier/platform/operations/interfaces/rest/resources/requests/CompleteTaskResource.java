package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CompleteTaskResource(
        @NotNull(message = "Las horas laboradas reales son obligatorias")
        @Positive(message = "Las horas laboradas deben ser mayores a cero")
        BigDecimal actualLaborHours,
        @Size(max = 1000, message = "Las notas técnicas no pueden superar 1000 caracteres")
        String notes
) {}
