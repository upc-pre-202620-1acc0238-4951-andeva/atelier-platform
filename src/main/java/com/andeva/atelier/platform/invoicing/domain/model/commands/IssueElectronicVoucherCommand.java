package com.andeva.atelier.platform.invoicing.domain.model.commands;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Command requesting formal issuance of an electronic voucher (Factura or Boleta).
 *
 * @author Joel Huamani Estefanero
 */
public record IssueElectronicVoucherCommand(
        TenantId tenantId,
        BranchId branchId,
        CustomerId customerId,
        Optional<WorkOrderId> workOrderId,
        VoucherType type,
        VoucherSerie serie,
        CustomerFiscalInfo customerInfo,
        Currency currency,
        List<VoucherLineCommandDto> lines
) implements Serializable {

    public IssueElectronicVoucherCommand {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(workOrderId, "Work order ID optional cannot be null");
        Objects.requireNonNull(type, "Voucher type cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(customerInfo, "Customer fiscal info cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(lines, "Lines cannot be null");
        lines = Collections.unmodifiableList(lines);
    }
}
