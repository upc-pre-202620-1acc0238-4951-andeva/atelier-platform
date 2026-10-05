package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTaskMechanicResource(
        @NotNull(message = "El identificador del mecánico es mandatorio")
        UUID mechanicId
) {}
