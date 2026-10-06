package com.andeva.atelier.platform.inventory.interfaces.rest.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreatePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.PurchaseOrderItem;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.CreatePurchaseOrderItemResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.CreatePurchaseOrderResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PurchaseOrderItemResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PurchaseOrderResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;

public final class PurchaseOrderResourceAssembler {

    private PurchaseOrderResourceAssembler() {
    }

    public static CreatePurchaseOrderCommand toCommand(TenantId tenantId, CreatePurchaseOrderResource resource) {
        List<CreatePurchaseOrderCommand.CreatePurchaseOrderItemVo> itemVos = resource.items().stream()
                .map(PurchaseOrderResourceAssembler::toItemVo)
                .toList();

        PurchaseOrderNumber orderNumber = resource.orderNumber() != null && !resource.orderNumber().isBlank()
                ? PurchaseOrderNumber.of(resource.orderNumber())
                : null;

        return new CreatePurchaseOrderCommand(
                tenantId,
                SupplierId.of(resource.supplierId()),
                BranchId.of(resource.branchId()),
                orderNumber,
                resource.notes(),
                resource.expectedDeliveryDate(),
                itemVos
        );
    }

    private static CreatePurchaseOrderCommand.CreatePurchaseOrderItemVo toItemVo(CreatePurchaseOrderItemResource resource) {
        return new CreatePurchaseOrderCommand.CreatePurchaseOrderItemVo(
                InventoryItemId.of(resource.itemId()),
                Quantity.of(resource.quantity()),
                Money.of(resource.unitCost(), Currency.PEN)
        );
    }

    public static PurchaseOrderResource toResource(PurchaseOrder order) {
        if (order == null) {
            return null;
        }

        List<PurchaseOrderItemResource> itemResources = order.getItems().stream()
                .map(PurchaseOrderResourceAssembler::toItemResource)
                .toList();

        return new PurchaseOrderResource(
                order.getId().value(),
                order.getTenantId().value(),
                order.getSupplierId().value(),
                order.getBranchId().value(),
                order.getOrderNumber().value(),
                order.getStatus(),
                order.getTotalCost().amount(),
                order.getReceiptImageUrl().map(StorageUrl::value).orElse(null),
                order.getReceiptNumber().orElse(null),
                order.getReceivedAt().orElse(null),
                itemResources
        );
    }

    public static PurchaseOrderItemResource toItemResource(PurchaseOrderItem line) {
        if (line == null) {
            return null;
        }

        return new PurchaseOrderItemResource(
                line.getId().value(),
                line.getItemId().value(),
                line.getQuantity().value(),
                line.getUnitCost().amount(),
                line.getTotalCost().amount()
        );
    }

    public static List<PurchaseOrderResource> toResourceList(List<PurchaseOrder> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream().map(PurchaseOrderResourceAssembler::toResource).toList();
    }
}
