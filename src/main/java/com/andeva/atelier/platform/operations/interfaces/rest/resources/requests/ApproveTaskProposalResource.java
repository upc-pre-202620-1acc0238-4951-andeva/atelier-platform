package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ApproveTaskProposalResource(
        @NotNull(message = "El servicio tarifario formal es obligatorio")
        UUID serviceId,
        @Positive(message = "El precio acordado debe ser positivo")
        BigDecimal finalPrice,
        @Positive(message = "Las horas estimadas deben ser positivas")
        BigDecimal laborHours,
        UUID mechanicId,
        @Size(max = 1000, message = "Las notas de concertación no pueden exceder 1000 caracteres")
        String notes
) {}
