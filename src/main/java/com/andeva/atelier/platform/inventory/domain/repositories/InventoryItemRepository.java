package com.andeva.atelier.platform.inventory.domain.repositories;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository {

    Optional<InventoryItem> findById(InventoryItemId id);

    Optional<InventoryItem> findByTenantIdAndSku(TenantId tenantId, Sku sku);

    boolean existsByTenantIdAndSku(TenantId tenantId, Sku sku);

    List<InventoryItem> findByTenantId(TenantId tenantId);

    List<InventoryItem> findByTenantIdAndCategory(TenantId tenantId, ItemCategory category);

    List<InventoryItem> findLowStockItems(TenantId tenantId);

    InventoryItem save(InventoryItem item);

    void delete(InventoryItem item);
}
