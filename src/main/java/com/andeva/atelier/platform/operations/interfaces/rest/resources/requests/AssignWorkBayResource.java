package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignWorkBayResource(
        @NotNull(message = "El identificador de la bahía es mandatorio")
        UUID bayId
) {}
