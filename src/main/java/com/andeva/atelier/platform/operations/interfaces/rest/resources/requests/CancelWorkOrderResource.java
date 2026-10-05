package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelWorkOrderResource(
        @NotBlank(message = "El motivo formal de anulación es obligatorio")
        @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres")
        String reason
) {}
