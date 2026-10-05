package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record AttachTaskEvidenceResource(
        @NotBlank(message = "La URL de la evidencia fotográfica es obligatoria")
        @URL(message = "Debe proporcionar una URL HTTPS válida de Firebase Storage")
        String imageUrl,
        @Size(max = 500, message = "La descripción de la evidencia no puede superar 500 caracteres")
        String description
) {}
