package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record AttachImageResource(
        @NotBlank(message = "La URL de la imagen en Firebase Storage es obligatoria")
        @URL(message = "Debe proporcionar una URL válida de almacenamiento HTTPS")
        String imageUrl,
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description
) {}
