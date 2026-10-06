package com.andeva.atelier.platform.hr.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PayrollPaymentResource(
        UUID id,
        UUID membershipId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal baseAmount,
        BigDecimal deductions,
        BigDecimal bonuses,
        BigDecimal totalPaid,
        String currency,
        String status,
        Instant paidAt,
        String paymentReference,
        List<PayrollItemResource> items
) {}
