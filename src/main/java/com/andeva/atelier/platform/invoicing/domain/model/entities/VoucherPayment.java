package com.andeva.atelier.platform.invoicing.domain.model.entities;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Child entity representing a cashier settlement or payment transaction amortizing an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public class VoucherPayment implements Serializable {

    private final PaymentId id;
    private final VoucherId voucherId;
    private final TenantId tenantId;
    private final BranchId branchId;
    private final Money amount;
    private final PaymentMethod paymentMethod;
    private final String transactionReference;
    private PaymentStatus status;
    private final Instant paidAt;

    public VoucherPayment(
            PaymentId id,
            VoucherId voucherId,
            TenantId tenantId,
            BranchId branchId,
            Money amount,
            PaymentMethod paymentMethod,
            String transactionReference,
            PaymentStatus status,
            Instant paidAt
    ) {
        this.id = Objects.requireNonNull(id, "Payment ID cannot be null");
        this.voucherId = Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "Branch ID cannot be null");
        this.amount = Objects.requireNonNull(amount, "Payment amount cannot be null");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        this.status = Objects.requireNonNull(status, "Payment status cannot be null");
        this.paidAt = Objects.requireNonNull(paidAt, "Paid at timestamp cannot be null");

        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidVoucherAmountException("Payment amount must be strictly greater than zero");
        }

        if (paymentMethod.requiresTransactionReference()) {
            if (transactionReference == null || transactionReference.trim().isBlank()) {
                throw new IllegalArgumentException(
                        "Transaction reference is required for payment method: " + paymentMethod.name()
                );
            }
            this.transactionReference = transactionReference.trim();
        } else {
            this.transactionReference = transactionReference != null ? transactionReference.trim() : "";
        }
    }

    public static VoucherPayment record(
            PaymentId paymentId,
            VoucherId voucherId,
            TenantId tenantId,
            BranchId branchId,
            Money amount,
            PaymentMethod method,
            String transactionReference
    ) {
        return new VoucherPayment(
                paymentId != null ? paymentId : PaymentId.generate(),
                voucherId,
                tenantId,
                branchId,
                amount,
                method,
                transactionReference,
                PaymentStatus.COMPLETED,
                Instant.now()
        );
    }

    public PaymentId getId() {
        return id;
    }

    public VoucherId getVoucherId() {
        return voucherId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public BranchId getBranchId() {
        return branchId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
}
