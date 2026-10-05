package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.InventoryItemPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryItemPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.InventoryItemPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InventoryItemRepositoryImpl implements InventoryItemRepository {

    private final InventoryItemPersistenceRepository persistenceRepository;

    public InventoryItemRepositoryImpl(InventoryItemPersistenceRepository persistenceRepository) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
    }

    @Override
    public Optional<InventoryItem> findById(InventoryItemId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value())
                .map(InventoryItemPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<InventoryItem> findByTenantIdAndSku(TenantId tenantId, Sku sku) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(sku, "sku cannot be null");
        return persistenceRepository.findByTenantIdAndSku(tenantId.value(), sku.value())
                .map(InventoryItemPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByTenantIdAndSku(TenantId tenantId, Sku sku) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(sku, "sku cannot be null");
        return persistenceRepository.existsByTenantIdAndSku(tenantId.value(), sku.value());
    }

    @Override
    public List<InventoryItem> findByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findByTenantId(tenantId.value()).stream()
                .map(InventoryItemPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<InventoryItem> findByTenantIdAndCategory(TenantId tenantId, ItemCategory category) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(category, "category cannot be null");
        return persistenceRepository.findByTenantIdAndCategory(tenantId.value(), category).stream()
                .map(InventoryItemPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<InventoryItem> findLowStockItems(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findLowStockItems(tenantId.value()).stream()
                .map(InventoryItemPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        InventoryItemPersistenceEntity entity = persistenceRepository.findById(item.getId().value())
                .map(existing -> {
                    InventoryItemPersistenceAssembler.updateEntity(existing, item);
                    return existing;
                })
                .orElseGet(() -> InventoryItemPersistenceAssembler.toEntity(item));

        InventoryItemPersistenceEntity saved = persistenceRepository.save(entity);
        return InventoryItemPersistenceAssembler.toDomain(saved);
    }

    @Override
    public void delete(InventoryItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        persistenceRepository.deleteById(item.getId().value());
    }
}
