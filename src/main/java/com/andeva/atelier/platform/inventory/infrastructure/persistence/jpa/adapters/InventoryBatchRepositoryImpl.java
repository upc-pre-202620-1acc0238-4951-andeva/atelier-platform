package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryBatchRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.InventoryBatchPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryBatchPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryItemPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.InventoryBatchPersistenceRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.InventoryItemPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InventoryBatchRepositoryImpl implements InventoryBatchRepository {

    private final InventoryBatchPersistenceRepository batchRepository;
    private final InventoryItemPersistenceRepository itemRepository;

    public InventoryBatchRepositoryImpl(
            InventoryBatchPersistenceRepository batchRepository,
            InventoryItemPersistenceRepository itemRepository
    ) {
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository cannot be null");
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository cannot be null");
    }

    @Override
    public Optional<InventoryBatch> findById(InventoryBatchId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return batchRepository.findById(id.value())
                .map(InventoryBatchPersistenceAssembler::toDomain);
    }

    @Override
    public List<InventoryBatch> findByItemId(InventoryItemId itemId) {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        return batchRepository.findByItemId(itemId.value()).stream()
                .map(InventoryBatchPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<InventoryBatch> findAvailableBatchesFifo(InventoryItemId itemId) {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        return batchRepository.findAvailableBatchesFifo(itemId.value()).stream()
                .map(InventoryBatchPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public InventoryBatch save(InventoryBatch batch) {
        Objects.requireNonNull(batch, "batch cannot be null");
        InventoryBatchPersistenceEntity entity = batchRepository.findById(batch.getId().value())
                .map(existing -> {
                    InventoryBatchPersistenceAssembler.updateEntity(existing, batch);
                    return existing;
                })
                .orElseGet(() -> {
                    InventoryItemPersistenceEntity itemEntity = itemRepository.findById(batch.getItemId().value())
                            .orElseThrow(() -> new IllegalArgumentException("InventoryItem not found: " + batch.getItemId().value()));
                    return InventoryBatchPersistenceAssembler.toEntity(batch, itemEntity);
                });

        InventoryBatchPersistenceEntity saved = batchRepository.save(entity);
        return InventoryBatchPersistenceAssembler.toDomain(saved);
    }

    @Override
    public List<InventoryBatch> saveAll(List<InventoryBatch> batches) {
        Objects.requireNonNull(batches, "batches cannot be null");
        return batches.stream().map(this::save).toList();
    }

    @Override
    public Money calculateTotalValuationByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        BigDecimal total = batchRepository.sumValuationByTenantId(tenantId.value());
        return Money.of(total != null ? total : BigDecimal.ZERO, Currency.PEN);
    }
}
