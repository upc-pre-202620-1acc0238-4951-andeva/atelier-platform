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
        @NotBlank(message = "{iam.validation.compensation.type.required}")
        @Pattern(regexp = "^(FIXED|HOURLY)$", message = "{iam.validation.compensation.type.format}")
        String salaryType,

        @NotNull(message = "{iam.validation.compensation.amount.required}")
        @DecimalMin(value = "0.00", message = "{iam.validation.compensation.amount.min}")
        BigDecimal baseSalary,

        @NotBlank(message = "{iam.validation.compensation.currency.required}")
        @Pattern(regexp = "^(PEN|USD)$", message = "{iam.validation.compensation.currency.format}")
        String currency
) {
}
