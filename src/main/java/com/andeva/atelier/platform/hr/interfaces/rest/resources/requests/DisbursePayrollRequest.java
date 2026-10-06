package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record DisbursePayrollRequest(
        @NotBlank String paymentReference,
        Instant paidAt
) {}
