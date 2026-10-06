package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

public record UpdateEmploymentStatusRequest(
        @NotBlank String employmentStatus
) {}
