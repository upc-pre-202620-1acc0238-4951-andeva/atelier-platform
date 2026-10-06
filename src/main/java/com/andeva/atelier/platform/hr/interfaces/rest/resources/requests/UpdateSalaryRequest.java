package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateSalaryRequest(
        @NotNull @Positive BigDecimal baseSalary,
        String currency,
        String compensationType
) {}
