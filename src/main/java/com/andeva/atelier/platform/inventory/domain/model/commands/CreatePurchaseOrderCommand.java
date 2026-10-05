package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record CreatePurchaseOrderCommand(
        TenantId tenantId,
        SupplierId supplierId,
        BranchId branchId,
        PurchaseOrderNumber orderNumber,
        String notes,
        LocalDate expectedDeliveryDate,
        List<CreatePurchaseOrderItemVo> items
) implements Serializable {

    public CreatePurchaseOrderCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(supplierId, "supplierId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
    }

    public record CreatePurchaseOrderItemVo(
            InventoryItemId itemId,
            Quantity quantity,
            Money unitCost
    ) implements Serializable {
        public CreatePurchaseOrderItemVo {
            Objects.requireNonNull(itemId, "itemId cannot be null");
            Objects.requireNonNull(quantity, "quantity cannot be null");
            Objects.requireNonNull(unitCost, "unitCost cannot be null");
        }
    }
}
