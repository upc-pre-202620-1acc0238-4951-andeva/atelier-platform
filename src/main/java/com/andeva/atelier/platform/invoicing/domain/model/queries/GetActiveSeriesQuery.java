package com.andeva.atelier.platform.invoicing.domain.model.queries;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Query requesting active series configurations for a branch and voucher type.
 *
 * @author Joel Huamani Estefanero
 */
public record GetActiveSeriesQuery(
        TenantId tenantId,
        BranchId branchId,
        VoucherType voucherType
) implements Serializable {

    public GetActiveSeriesQuery {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(voucherType, "Voucher type cannot be null");
    }
}
