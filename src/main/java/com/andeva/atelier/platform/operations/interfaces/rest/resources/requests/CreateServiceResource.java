package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateServiceResource(
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 150, message = "El nombre del servicio no puede superar 150 caracteres")
        String name,
        @NotNull(message = "La tarifa base es obligatoria")
        @Positive(message = "La tarifa base debe ser estrictamente positiva")
        BigDecimal basePrice,
        @NotBlank(message = "La divisa es obligatoria")
        @Pattern(regexp = "PEN|USD", message = "La divisa debe ser PEN o USD")
        String currency,
        @Positive(message = "El tiempo estimado en minutos debe ser positivo")
        int estimatedMinutes
) {}
