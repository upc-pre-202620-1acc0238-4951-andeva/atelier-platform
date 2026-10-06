package com.andeva.atelier.platform.inventory.application.internal.queryservices;

import com.andeva.atelier.platform.inventory.application.queryservices.InventoryItemQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryBatchesByItemIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemBySkuQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsPagedQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryValuationQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetLowStockItemsQuery;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryBatchRepository;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.InventoryItemPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.InventoryItemPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class InventoryItemQueryServiceImpl implements InventoryItemQueryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryBatchRepository batchRepository;
    private final InventoryItemPersistenceRepository itemPersistenceRepository;

    public InventoryItemQueryServiceImpl(
            InventoryItemRepository itemRepository,
            InventoryBatchRepository batchRepository,
            InventoryItemPersistenceRepository itemPersistenceRepository
    ) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository cannot be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository cannot be null");
        this.itemPersistenceRepository = Objects.requireNonNull(itemPersistenceRepository, "itemPersistenceRepository cannot be null");
    }

    @Override
    public Optional<InventoryItem> handle(GetInventoryItemByIdQuery query) {
        Objects.requireNonNull(query, "GetInventoryItemByIdQuery cannot be null");
        return itemRepository.findById(query.itemId());
    }

    @Override
    public Optional<InventoryItem> handle(GetInventoryItemBySkuQuery query) {
        Objects.requireNonNull(query, "GetInventoryItemBySkuQuery cannot be null");
        return itemRepository.findByTenantIdAndSku(query.tenantId(), query.sku());
    }

    @Override
    public List<InventoryItem> handle(GetInventoryItemsByTenantIdQuery query) {
        Objects.requireNonNull(query, "GetInventoryItemsByTenantIdQuery cannot be null");
        if (query.category() != null) {
            return itemRepository.findByTenantIdAndCategory(query.tenantId(), query.category());
        }
        return itemRepository.findByTenantId(query.tenantId());
    }

    @Override
    public Page<InventoryItem> handle(GetInventoryItemsPagedQuery query) {
        Objects.requireNonNull(query, "GetInventoryItemsPagedQuery cannot be null");
        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by("name").ascending());
        return itemPersistenceRepository.findPaged(
                query.tenantId().value(),
                query.category(),
                query.search(),
                pageable
        ).map(InventoryItemPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<InventoryItem> handle(GetInventoryItemDetailQuery query) {
        Objects.requireNonNull(query, "GetInventoryItemDetailQuery cannot be null");
        return itemRepository.findById(query.itemId());
    }

    @Override
    public List<InventoryBatch> handle(GetInventoryBatchesByItemIdQuery query) {
        Objects.requireNonNull(query, "GetInventoryBatchesByItemIdQuery cannot be null");
        return batchRepository.findByItemId(query.itemId());
    }

    @Override
    public List<InventoryItem> handle(GetLowStockItemsQuery query) {
        Objects.requireNonNull(query, "GetLowStockItemsQuery cannot be null");
        return itemRepository.findLowStockItems(query.tenantId());
    }

    @Override
    public Money handle(GetInventoryValuationQuery query) {
        Objects.requireNonNull(query, "GetInventoryValuationQuery cannot be null");
        return batchRepository.calculateTotalValuationByTenantId(query.tenantId());
    }
}
