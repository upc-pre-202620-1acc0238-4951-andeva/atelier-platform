package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;
import java.util.Objects;

public record ListPayrollPaymentsByPeriodQuery(
        TenantId tenantId,
        LocalDate periodStart,
        LocalDate periodEnd,
        String status
) {
    public ListPayrollPaymentsByPeriodQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
    }
}
