package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetPurchaseOrdersByTenantIdQuery(
        TenantId tenantId,
        BranchId branchId,
        PurchaseOrderStatus status
) {
    public GetPurchaseOrdersByTenantIdQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }

    public GetPurchaseOrdersByTenantIdQuery(TenantId tenantId) {
        this(tenantId, null, null);
    }
}
