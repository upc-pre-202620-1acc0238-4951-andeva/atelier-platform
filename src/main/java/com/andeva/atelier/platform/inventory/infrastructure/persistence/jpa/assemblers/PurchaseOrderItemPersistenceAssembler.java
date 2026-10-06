package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.entities.PurchaseOrderItem;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.PurchaseOrderItemPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.PurchaseOrderPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

public final class PurchaseOrderItemPersistenceAssembler {

    private PurchaseOrderItemPersistenceAssembler() {
    }

    public static PurchaseOrderItem toDomain(PurchaseOrderItemPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return PurchaseOrderItem.reconstitute(
                PurchaseOrderItemId.of(entity.getId()),
                PurchaseOrderId.of(entity.getPurchaseOrder().getId()),
                InventoryItemId.of(entity.getItemId()),
                Quantity.of(entity.getQuantity()),
                Money.of(entity.getUnitCost(), Currency.PEN),
                Money.of(entity.getTotalCost(), Currency.PEN)
        );
    }

    public static PurchaseOrderItemPersistenceEntity toEntity(PurchaseOrderItem domain, PurchaseOrderPersistenceEntity orderEntity) {
        if (domain == null) {
            return null;
        }

        return new PurchaseOrderItemPersistenceEntity(
                domain.getId().value(),
                orderEntity,
                domain.getItemId().value(),
                domain.getQuantity().value(),
                domain.getUnitCost().amount(),
                domain.getTotalCost().amount()
        );
    }

    public static void updateEntity(PurchaseOrderItemPersistenceEntity entity, PurchaseOrderItem domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setQuantity(domain.getQuantity().value());
        entity.setUnitCost(domain.getUnitCost().amount());
        entity.setTotalCost(domain.getTotalCost().amount());
    }
}
