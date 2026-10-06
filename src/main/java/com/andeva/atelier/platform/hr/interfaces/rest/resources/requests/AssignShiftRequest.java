package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignShiftRequest(
        @NotNull UUID shiftId
) {}
