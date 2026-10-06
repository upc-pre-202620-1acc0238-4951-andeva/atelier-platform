package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddPayrollBonusRequest(
        @NotBlank @Size(max = 150) String concept,
        @NotNull @Positive BigDecimal amount,
        String currency,
        @NotBlank String bonusType,
        @NotNull LocalDate date
) {}
