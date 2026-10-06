package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record DisbursePayrollPaymentCommand(
        TenantId tenantId,
        PayrollPaymentId payrollId,
        String paymentReference,
        Instant paidAt
) {
    public DisbursePayrollPaymentCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(payrollId, "PayrollPaymentId cannot be null");
        Objects.requireNonNull(paymentReference, "paymentReference cannot be null");
    }
}
