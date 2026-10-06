package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryBatchPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryItemPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class InventoryItemPersistenceAssembler {

    private InventoryItemPersistenceAssembler() {
    }

    public static InventoryItem toDomain(InventoryItemPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        List<InventoryBatch> domainBatches = entity.getBatches() != null
                ? entity.getBatches().stream().map(InventoryBatchPersistenceAssembler::toDomain).toList()
                : List.of();

        return InventoryItem.reconstitute(
                InventoryItemId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                Sku.of(entity.getSku()),
                entity.getCategory(),
                Money.of(entity.getBasePrice(), Currency.PEN),
                Quantity.of(entity.getTotalStock()),
                Quantity.of(entity.getMinimumStock()),
                "UNIT",
                entity.getStatus(),
                domainBatches
        );
    }

    public static InventoryItemPersistenceEntity toEntity(InventoryItem domain) {
        if (domain == null) {
            return null;
        }

        InventoryItemPersistenceEntity entity = new InventoryItemPersistenceEntity(
                domain.getId().value(),
                domain.getTenantId().value(),
                domain.getName(),
                domain.getSku().value(),
                domain.getCategory(),
                domain.getBasePrice().amount(),
                domain.getTotalStock().value(),
                domain.getMinimumStock().value(),
                domain.getStatus()
        );

        if (domain.getBatches() != null) {
            for (InventoryBatch batch : domain.getBatches()) {
                InventoryBatchPersistenceEntity batchEntity = InventoryBatchPersistenceAssembler.toEntity(batch, entity);
                entity.addBatch(batchEntity);
            }
        }

        return entity;
    }

    public static void updateEntity(InventoryItemPersistenceEntity entity, InventoryItem domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setName(domain.getName());
        entity.setCategory(domain.getCategory());
        entity.setBasePrice(domain.getBasePrice().amount());
        entity.setTotalStock(domain.getTotalStock().value());
        entity.setMinimumStock(domain.getMinimumStock().value());
        entity.setStatus(domain.getStatus());

        // Synchronize batches
        Map<UUID, InventoryBatchPersistenceEntity> existingBatches = entity.getBatches().stream()
                .collect(Collectors.toMap(InventoryBatchPersistenceEntity::getId, b -> b));

        for (InventoryBatch domainBatch : domain.getBatches()) {
            InventoryBatchPersistenceEntity existing = existingBatches.get(domainBatch.getId().value());
            if (existing != null) {
                InventoryBatchPersistenceAssembler.updateEntity(existing, domainBatch);
            } else {
                InventoryBatchPersistenceEntity newBatchEntity = InventoryBatchPersistenceAssembler.toEntity(domainBatch, entity);
                entity.addBatch(newBatchEntity);
            }
        }
    }
}
