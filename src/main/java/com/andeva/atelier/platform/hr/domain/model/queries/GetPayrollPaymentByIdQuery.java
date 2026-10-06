package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetPayrollPaymentByIdQuery(TenantId tenantId, PayrollPaymentId payrollId) {
    public GetPayrollPaymentByIdQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(payrollId, "PayrollPaymentId cannot be null");
    }
}
