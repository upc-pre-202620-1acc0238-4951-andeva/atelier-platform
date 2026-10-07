package com.andeva.atelier.platform.invoicing.domain.model.commands;

import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Command requesting issuance of an electronic credit note referencing a previously issued voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record IssueCreditNoteCommand(
        TenantId tenantId,
        BranchId branchId,
        CustomerId customerId,
        VoucherId referenceVoucherId,
        VoucherSerie serie,
        CreditNoteReason reason,
        String reasonDescription,
        List<VoucherLineCommandDto> lines
) implements Serializable {

    public IssueCreditNoteCommand {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(referenceVoucherId, "Reference voucher ID cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(reason, "Credit note reason cannot be null");
        Objects.requireNonNull(lines, "Lines cannot be null");
        lines = Collections.unmodifiableList(lines);
    }
}
