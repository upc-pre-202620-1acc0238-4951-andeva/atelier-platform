package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Request payload for modifying the contractual salary compensation scheme of a staff member.
 *
 * @param salaryType Remuneration scheme (FIXED monthly or HOURLY wage)
 * @param baseSalary Base nominal compensation amount
 * @param currency   ISO-4217 currency code (PEN or USD)
 * @author Joel Huamani Estefanero
 */
public record UpdateCompensationResource(
        @NotBlank @Pattern(regexp = "^(FIXED|HOURLY)$")
        String salaryType,

        @NotNull @DecimalMin("0.00")
        BigDecimal baseSalary,

        @NotBlank @Pattern(regexp = "^(PEN|USD)$")
        String currency
) {
}
