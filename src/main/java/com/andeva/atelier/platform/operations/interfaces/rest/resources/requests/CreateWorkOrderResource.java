package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateWorkOrderResource(
        UUID appointmentId,
        @NotNull(message = "El identificador del vehículo es mandatorio")
        UUID vehicleId,
        UUID customerId,
        UUID branchId,
        @NotNull(message = "El kilometraje de ingreso es obligatorio")
        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        Integer mileageIn,
        @Size(max = 2000, message = "El diagnóstico de recepción no puede superar 2000 caracteres")
        String diagnosticSummary
) {
    public CreateWorkOrderResource(UUID appointmentId, UUID vehicleId, Integer mileageIn, String diagnosticSummary) {
        this(appointmentId, vehicleId, null, null, mileageIn, diagnosticSummary);
    }
}
