package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record PayrollCalculatedEvent(
        PayrollPaymentId payrollPaymentId,
        TenantId tenantId,
        TenantMembershipId membershipId,
        Money totalPaid,
        Instant occurredOn
) implements Serializable {

    public PayrollCalculatedEvent {
        Objects.requireNonNull(payrollPaymentId, "payrollPaymentId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(totalPaid, "totalPaid cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static PayrollCalculatedEvent now(
            PayrollPaymentId payrollPaymentId,
            TenantId tenantId,
            TenantMembershipId membershipId,
            Money totalPaid
    ) {
        return new PayrollCalculatedEvent(payrollPaymentId, tenantId, membershipId, totalPaid, Instant.now());
    }
}
