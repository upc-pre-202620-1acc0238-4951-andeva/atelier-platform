package com.andeva.atelier.platform.inventory.domain.repositories;

import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

public interface InventoryBatchRepository {

    Optional<InventoryBatch> findById(InventoryBatchId id);

    List<InventoryBatch> findByItemId(InventoryItemId itemId);

    List<InventoryBatch> findAvailableBatchesFifo(InventoryItemId itemId);

    InventoryBatch save(InventoryBatch batch);

    List<InventoryBatch> saveAll(List<InventoryBatch> batches);

    Money calculateTotalValuationByTenantId(TenantId tenantId);
}
