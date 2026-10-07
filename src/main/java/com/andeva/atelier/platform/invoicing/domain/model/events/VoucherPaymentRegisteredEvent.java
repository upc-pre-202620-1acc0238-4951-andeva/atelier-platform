package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a financial payment or cash settlement is recorded against an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherPaymentRegisteredEvent(
        PaymentId paymentId,
        VoucherId voucherId,
        TenantId tenantId,
        BranchId branchId,
        Money amount,
        PaymentMethod paymentMethod,
        boolean isFullyPaid,
        Instant occurredOn
) implements Serializable {

    public VoucherPaymentRegisteredEvent {
        Objects.requireNonNull(paymentId, "Payment ID cannot be null");
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(amount, "Payment amount cannot be null");
        Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    /**
     * Backward-compatible alias for paymentMethod().
     */
    public PaymentMethod method() {
        return paymentMethod;
    }

    public static VoucherPaymentRegisteredEvent of(
            PaymentId paymentId,
            VoucherId voucherId,
            TenantId tenantId,
            BranchId branchId,
            Money amount,
            PaymentMethod paymentMethod,
            boolean isFullyPaid
    ) {
        return new VoucherPaymentRegisteredEvent(paymentId, voucherId, tenantId, branchId, amount, paymentMethod, isFullyPaid, Instant.now());
    }

    public static VoucherPaymentRegisteredEvent of(
            PaymentId paymentId,
            VoucherId voucherId,
            TenantId tenantId,
            Money amount,
            PaymentMethod paymentMethod,
            boolean isFullyPaid
    ) {
        // Fallback for tests or contexts where branchId is not yet wired
        return new VoucherPaymentRegisteredEvent(paymentId, voucherId, tenantId, BranchId.generate(), amount, paymentMethod, isFullyPaid, Instant.now());
    }
}
