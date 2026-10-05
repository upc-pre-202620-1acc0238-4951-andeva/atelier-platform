package com.andeva.atelier.platform.inventory.application.queryservices;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryBatchesByItemIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsPagedQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryValuationQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetLowStockItemsQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface InventoryItemQueryService {

    Optional<InventoryItem> handle(GetInventoryItemByIdQuery query);

    List<InventoryItem> handle(GetInventoryItemsByTenantIdQuery query);

    Page<InventoryItem> handle(GetInventoryItemsPagedQuery query);

    Optional<InventoryItem> handle(GetInventoryItemDetailQuery query);

    List<InventoryBatch> handle(GetInventoryBatchesByItemIdQuery query);

    List<InventoryItem> handle(GetLowStockItemsQuery query);

    Money handle(GetInventoryValuationQuery query);
}
