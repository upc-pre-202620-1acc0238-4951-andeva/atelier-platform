package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryBatchPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryItemPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public final class InventoryBatchPersistenceAssembler {

    private InventoryBatchPersistenceAssembler() {
    }

    public static InventoryBatch toDomain(InventoryBatchPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return InventoryBatch.reconstitute(
                InventoryBatchId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                InventoryItemId.of(entity.getItem().getId()),
                entity.getSupplierId() != null ? SupplierId.of(entity.getSupplierId()) : null,
                entity.getPurchaseOrderId() != null ? PurchaseOrderId.of(entity.getPurchaseOrderId()) : null,
                entity.getBatchNumber(),
                Quantity.of(entity.getInitialQty()),
                Quantity.of(entity.getRemainingQty()),
                Money.of(entity.getUnitCost(), Currency.PEN),
                entity.getArrivalDate(),
                entity.getReceiptImageUrl() != null ? StorageUrl.of(entity.getReceiptImageUrl()) : null
        );
    }

    public static InventoryBatchPersistenceEntity toEntity(InventoryBatch domain, InventoryItemPersistenceEntity itemEntity) {
        if (domain == null) {
            return null;
        }

        return new InventoryBatchPersistenceEntity(
                domain.getId().value(),
                domain.getTenantId().value(),
                itemEntity,
                domain.getSupplierId().map(SupplierId::value).orElse(null),
                domain.getPurchaseOrderId().map(PurchaseOrderId::value).orElse(null),
                domain.getBatchNumber(),
                domain.getReceiptImageUrl().map(StorageUrl::value).orElse(null),
                domain.getInitialQuantity().value(),
                domain.getRemainingQuantity().value(),
                domain.getUnitCost().amount(),
                domain.getArrivalDate()
        );
    }

    public static void updateEntity(InventoryBatchPersistenceEntity entity, InventoryBatch domain) {
        if (entity == null || domain == null) {
            return;
        }
        entity.setRemainingQty(domain.getRemainingQuantity().value());
        entity.setReceiptImageUrl(domain.getReceiptImageUrl().map(StorageUrl::value).orElse(null));
    }
}
