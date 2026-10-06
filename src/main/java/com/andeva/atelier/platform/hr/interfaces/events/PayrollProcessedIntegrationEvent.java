package com.andeva.atelier.platform.hr.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayrollProcessedIntegrationEvent(
        UUID payrollPaymentId,
        UUID tenantId,
        UUID membershipId,
        BigDecimal totalPaid,
        String currency,
        String paymentReference,
        Instant occurredOn
) {}
