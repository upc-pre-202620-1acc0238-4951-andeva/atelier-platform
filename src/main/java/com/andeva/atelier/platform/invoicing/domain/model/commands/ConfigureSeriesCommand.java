package com.andeva.atelier.platform.invoicing.domain.model.commands;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Command requesting configuration of an authorized fiscal series for a branch.
 *
 * @author Joel Huamani Estefanero
 */
public record ConfigureSeriesCommand(
        TenantId tenantId,
        BranchId branchId,
        VoucherType type,
        VoucherSerie serie,
        int initialCorrelative
) implements Serializable {

    public ConfigureSeriesCommand {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(type, "Voucher type cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
    }
}
