package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record PayrollDisbursedEvent(
        PayrollPaymentId payrollPaymentId,
        TenantId tenantId,
        TenantMembershipId membershipId,
        String paymentReference,
        Money totalPaid,
        Instant paidAt,
        Instant occurredOn
) implements Serializable {

    public PayrollDisbursedEvent {
        Objects.requireNonNull(payrollPaymentId, "payrollPaymentId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(paymentReference, "paymentReference cannot be null");
        Objects.requireNonNull(totalPaid, "totalPaid cannot be null");
        Objects.requireNonNull(paidAt, "paidAt cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static PayrollDisbursedEvent now(
            PayrollPaymentId payrollPaymentId,
            TenantId tenantId,
            TenantMembershipId membershipId,
            String paymentReference,
            Money totalPaid,
            Instant paidAt
    ) {
        return new PayrollDisbursedEvent(payrollPaymentId, tenantId, membershipId, paymentReference, totalPaid, paidAt, Instant.now());
    }
}
