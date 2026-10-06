package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GeneratePayrollRequest(
        @NotNull UUID membershipId,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd
) {}
