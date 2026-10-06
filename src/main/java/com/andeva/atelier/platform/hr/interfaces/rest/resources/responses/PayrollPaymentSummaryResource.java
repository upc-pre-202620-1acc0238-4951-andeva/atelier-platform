package com.andeva.atelier.platform.hr.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollPaymentSummaryResource(
        UUID id,
        UUID membershipId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal baseAmount,
        BigDecimal totalPaid,
        String currency,
        String status
) {}
