package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PauseTaskResource(
        @NotBlank(message = "El motivo de la pausa es obligatorio")
        @Pattern(regexp = "BREAK|END_OF_SHIFT", message = "El motivo debe ser BREAK o END_OF_SHIFT")
        String reason
) {}
