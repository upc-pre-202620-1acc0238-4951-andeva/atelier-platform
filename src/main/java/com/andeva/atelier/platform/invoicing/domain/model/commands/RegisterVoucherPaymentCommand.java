package com.andeva.atelier.platform.invoicing.domain.model.commands;

import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Command requesting registration of an amortizing payment or cash collection against an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterVoucherPaymentCommand(
        VoucherId voucherId,
        TenantId tenantId,
        BranchId branchId,
        Money amount,
        PaymentMethod method,
        String transactionReference
) implements Serializable {

    public RegisterVoucherPaymentCommand {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(method, "Payment method cannot be null");
    }

    public RegisterVoucherPaymentCommand(VoucherId voucherId, Money amount, PaymentMethod method, String transactionReference) {
        this(voucherId, null, null, amount, method, transactionReference);
    }
}
