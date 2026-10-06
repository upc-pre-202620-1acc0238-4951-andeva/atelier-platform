package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterEmployeeProfileRequest(
        @NotNull UUID branchId,
        @NotNull UUID membershipId,
        UUID shiftId,
        @NotNull @Positive BigDecimal baseSalary,
        String currency,
        String compensationType,
        @NotBlank @Size(max = 100) String jobTitle
) {}
