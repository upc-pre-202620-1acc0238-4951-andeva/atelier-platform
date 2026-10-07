package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when an electronic credit note is issued.
 *
 * @author Joel Huamani Estefanero
 */
public record CreditNoteIssuedEvent(
        VoucherId creditNoteId,
        VoucherId referenceVoucherId,
        TenantId tenantId,
        BranchId branchId,
        VoucherSerie serie,
        VoucherNumber number,
        CreditNoteReason reason,
        Money totalAmount,
        Instant occurredOn
) implements Serializable {

    public CreditNoteIssuedEvent {
        Objects.requireNonNull(creditNoteId, "Credit note ID cannot be null");
        Objects.requireNonNull(referenceVoucherId, "Reference voucher ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(number, "Voucher number cannot be null");
        Objects.requireNonNull(reason, "Credit note reason cannot be null");
        Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    public static CreditNoteIssuedEvent of(
            VoucherId creditNoteId,
            VoucherId referenceVoucherId,
            TenantId tenantId,
            BranchId branchId,
            VoucherSerie serie,
            VoucherNumber number,
            CreditNoteReason reason,
            Money totalAmount
    ) {
        return new CreditNoteIssuedEvent(creditNoteId, referenceVoucherId, tenantId, branchId, serie, number, reason, totalAmount, Instant.now());
    }
}
