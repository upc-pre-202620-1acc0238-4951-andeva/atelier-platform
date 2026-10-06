package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.entities.PurchaseOrderItem;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.PurchaseOrderItemPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.PurchaseOrderPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PurchaseOrderPersistenceAssembler {

    private PurchaseOrderPersistenceAssembler() {
    }

    public static PurchaseOrder toDomain(PurchaseOrderPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        List<PurchaseOrderItem> domainItems = entity.getItems() != null
                ? entity.getItems().stream().map(PurchaseOrderItemPersistenceAssembler::toDomain).toList()
                : List.of();

        return PurchaseOrder.reconstitute(
                PurchaseOrderId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                SupplierId.of(entity.getSupplierId()),
                BranchId.of(entity.getBranchId()),
                PurchaseOrderNumber.of(entity.getOrderNumber()),
                entity.getStatus(),
                Money.of(entity.getTotalCost(), Currency.PEN),
                entity.getReceiptImageUrl() != null ? StorageUrl.of(entity.getReceiptImageUrl()) : null,
                entity.getReceiptNumber(),
                entity.getReceivedAt(),
                domainItems
        );
    }

    public static PurchaseOrderPersistenceEntity toEntity(PurchaseOrder domain) {
        if (domain == null) {
            return null;
        }

        PurchaseOrderPersistenceEntity entity = new PurchaseOrderPersistenceEntity(
                domain.getId().value(),
                domain.getTenantId().value(),
                domain.getSupplierId().value(),
                domain.getBranchId().value(),
                domain.getOrderNumber().value(),
                domain.getStatus(),
                domain.getTotalCost().amount(),
                domain.getReceiptImageUrl().map(StorageUrl::value).orElse(null),
                domain.getReceiptNumber().orElse(null),
                domain.getReceivedAt().orElse(null)
        );

        if (domain.getItems() != null) {
            for (PurchaseOrderItem item : domain.getItems()) {
                PurchaseOrderItemPersistenceEntity itemEntity = PurchaseOrderItemPersistenceAssembler.toEntity(item, entity);
                entity.addItem(itemEntity);
            }
        }

        return entity;
    }

    public static void updateEntity(PurchaseOrderPersistenceEntity entity, PurchaseOrder domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setStatus(domain.getStatus());
        entity.setTotalCost(domain.getTotalCost().amount());
        entity.setReceiptImageUrl(domain.getReceiptImageUrl().map(StorageUrl::value).orElse(null));
        entity.setReceiptNumber(domain.getReceiptNumber().orElse(null));
        entity.setReceivedAt(domain.getReceivedAt().orElse(null));

        // Synchronize lines
        Map<UUID, PurchaseOrderItemPersistenceEntity> existingItems = entity.getItems().stream()
                .collect(Collectors.toMap(PurchaseOrderItemPersistenceEntity::getId, i -> i));

        for (PurchaseOrderItem domainItem : domain.getItems()) {
            PurchaseOrderItemPersistenceEntity existing = existingItems.get(domainItem.getId().value());
            if (existing != null) {
                PurchaseOrderItemPersistenceAssembler.updateEntity(existing, domainItem);
            } else {
                PurchaseOrderItemPersistenceEntity newItemEntity = PurchaseOrderItemPersistenceAssembler.toEntity(domainItem, entity);
                entity.addItem(newItemEntity);
            }
        }

        // Remove deleted items
        List<UUID> domainItemIds = domain.getItems().stream().map(i -> i.getId().value()).toList();
        entity.getItems().removeIf(item -> !domainItemIds.contains(item.getId()));
    }
}
