package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain event emitted when an electronic voucher is formally issued by the workshop.
 *
 * @author Joel Huamani Estefanero
 */
public record ElectronicVoucherIssuedEvent(
        VoucherId voucherId,
        TenantId tenantId,
        BranchId branchId,
        CustomerId customerId,
        Optional<WorkOrderId> workOrderId,
        VoucherType voucherType,
        VoucherSerie serie,
        VoucherNumber number,
        Money totalAmount,
        Currency currency,
        Instant occurredOn
) implements Serializable {

    public ElectronicVoucherIssuedEvent {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(workOrderId, "Work order ID optional cannot be null");
        Objects.requireNonNull(voucherType, "Voucher type cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(number, "Voucher number cannot be null");
        Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    /**
     * Backward-compatible alias for voucherType().
     */
    public VoucherType type() {
        return voucherType;
    }

    public static ElectronicVoucherIssuedEvent of(
            VoucherId voucherId,
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            WorkOrderId workOrderId,
            VoucherType voucherType,
            VoucherSerie serie,
            VoucherNumber number,
            Money totalAmount,
            Currency currency
    ) {
        return new ElectronicVoucherIssuedEvent(
                voucherId,
                tenantId,
                branchId,
                customerId,
                Optional.ofNullable(workOrderId),
                voucherType,
                serie,
                number,
                totalAmount,
                currency != null ? currency : totalAmount.currency(),
                Instant.now()
        );
    }

    public static ElectronicVoucherIssuedEvent of(
            VoucherId voucherId,
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            VoucherType voucherType,
            VoucherSerie serie,
            VoucherNumber number,
            Money totalAmount
    ) {
        return of(voucherId, tenantId, branchId, customerId, null, voucherType, serie, number, totalAmount, totalAmount.currency());
    }
}
