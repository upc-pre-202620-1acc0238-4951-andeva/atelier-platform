package com.andeva.atelier.platform.invoicing.interfaces.acl.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO for generating an electronic voucher from an MRO Work Order.
 *
 * @author Joel Huamani Estefanero
 */
public record GenerateVoucherFromWorkOrderCommandDto(
        UUID tenantId,
        UUID branchId,
        UUID workOrderId,
        UUID customerId,
        String voucherType,
        String customerTaxId,
        String customerLegalName,
        String customerFiscalAddress,
        String currency,
        List<WorkOrderItemBillingDto> items
) {
    public GenerateVoucherFromWorkOrderCommandDto {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(workOrderId, "WorkOrderId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(voucherType, "VoucherType cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(items, "Billing items cannot be null");
    }
}
