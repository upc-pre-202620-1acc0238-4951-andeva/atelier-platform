package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateWorkBayResource(
        @NotNull(message = "La sucursal de pertenencia es obligatoria")
        UUID branchId,
        @NotBlank(message = "El nombre identificador de la bahía es mandatorio")
        @Size(max = 100, message = "El nombre de la bahía no puede exceder 100 caracteres")
        String name,
        @NotBlank(message = "La tipología de bahía es mandatoria")
        @Pattern(regexp = "LIFT|PAINT_BOOTH|WASHING|ALIGNMENT|MECHANICAL_LIFT|WASH_BAY|DIAGNOSTIC_PIT|ALIGNMENT_STATION",
                message = "Tipología de bahía no reconocida")
        String bayType
) {}
