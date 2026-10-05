package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaintenanceBayResource(
        @NotBlank(message = "La justificación de mantenimiento es mandatoria")
        @Size(max = 1000, message = "La justificación no puede exceder 1000 caracteres")
        String reason
) {}
