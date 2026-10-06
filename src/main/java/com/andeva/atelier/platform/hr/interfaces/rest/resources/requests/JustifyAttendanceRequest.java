package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JustifyAttendanceRequest(
        @NotBlank @Size(max = 500) String reason
) {}
