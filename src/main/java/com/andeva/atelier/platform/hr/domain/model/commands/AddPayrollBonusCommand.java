package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;
import java.util.Objects;

public record AddPayrollBonusCommand(
        TenantId tenantId,
        PayrollPaymentId payrollId,
        String concept,
        Money amount,
        String bonusType,
        LocalDate date
) {
    public AddPayrollBonusCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(payrollId, "PayrollPaymentId cannot be null");
        Objects.requireNonNull(concept, "concept cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(bonusType, "bonusType cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
    }
}
