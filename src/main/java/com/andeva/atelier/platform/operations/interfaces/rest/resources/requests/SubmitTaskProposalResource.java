package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

public record SubmitTaskProposalResource(
        @NotNull(message = "El identificador del mecánico es obligatorio")
        UUID mechanicId,
        @NotBlank(message = "La descripción del hallazgo es obligatoria")
        @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
        String description,
        @NotBlank(message = "La severidad técnica es obligatoria")
        @Pattern(regexp = "LOW|MEDIUM|CRITICAL", message = "La severidad debe ser LOW, MEDIUM o CRITICAL")
        String severity,
        @NotBlank(message = "La URL de la fotografía pericial es obligatoria")
        @URL(message = "Debe proporcionar una URL válida de Firebase Storage")
        String imageUrl,
        UUID suggestedServiceId
) {}
