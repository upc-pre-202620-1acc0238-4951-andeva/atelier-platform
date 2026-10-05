package com.andeva.atelier.platform.operations.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectTaskProposalResource(
        @NotBlank(message = "El motivo de desestimación es obligatorio")
        @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres")
        String customerNotes
) {}
