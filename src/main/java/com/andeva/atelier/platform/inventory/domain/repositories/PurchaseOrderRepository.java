package com.andeva.atelier.platform.inventory.domain.repositories;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository {

    Optional<PurchaseOrder> findById(PurchaseOrderId id);

    Optional<PurchaseOrder> findByTenantIdAndOrderNumber(TenantId tenantId, PurchaseOrderNumber orderNumber);

    List<PurchaseOrder> findByTenantId(TenantId tenantId);

    List<PurchaseOrder> findByTenantIdAndBranchId(TenantId tenantId, BranchId branchId);

    List<PurchaseOrder> findByTenantIdAndStatus(TenantId tenantId, PurchaseOrderStatus status);

    PurchaseOrder save(PurchaseOrder order);

    void delete(PurchaseOrder order);
}
