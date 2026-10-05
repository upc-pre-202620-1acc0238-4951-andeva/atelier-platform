package com.andeva.atelier.platform.inventory.application.queryservices;

import com.andeva.atelier.platform.inventory.application.internal.queryservices.InventoryItemQueryServiceImpl;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryBatchesByItemIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemBySkuQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryValuationQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetLowStockItemsQuery;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryBatchRepository;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.InventoryItemPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryItem Query Service Tests")
class InventoryItemQueryServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;
    @Mock
    private InventoryBatchRepository batchRepository;
    @Mock
    private InventoryItemPersistenceRepository itemPersistenceRepository;

    private InventoryItemQueryServiceImpl queryService;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final Sku sku = Sku.of("BRK-PAD-001");
    private final InventoryItemId itemId = InventoryItemId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        queryService = new InventoryItemQueryServiceImpl(itemRepository, batchRepository, itemPersistenceRepository);
    }

    @Test
    @DisplayName("Should query item by SKU")
    void shouldQueryItemBySku() {
        InventoryItem item = InventoryItem.create(
                tenantId, "Pastillas Freno Bosch", sku, ItemCategory.BRAKES,
                Money.of(new BigDecimal("150.00"), Currency.PEN),
                Quantity.of(new BigDecimal("4.00")), "SET"
        );

        when(itemRepository.findByTenantIdAndSku(tenantId, sku)).thenReturn(Optional.of(item));

        Optional<InventoryItem> result = queryService.handle(new GetInventoryItemBySkuQuery(tenantId, sku));

        assertThat(result).isPresent();
        assertThat(result.get().getSku()).isEqualTo(sku);
        verify(itemRepository).findByTenantIdAndSku(tenantId, sku);
    }

    @Test
    @DisplayName("Should query item by ID")
    void shouldQueryItemById() {
        InventoryItem item = InventoryItem.create(
                tenantId, "Pastillas Freno Bosch", sku, ItemCategory.BRAKES,
                Money.of(new BigDecimal("150.00"), Currency.PEN),
                Quantity.of(new BigDecimal("4.00")), "SET"
        );

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Optional<InventoryItem> result = queryService.handle(new GetInventoryItemByIdQuery(itemId));

        assertThat(result).isPresent();
        verify(itemRepository).findById(itemId);
    }

    @Test
    @DisplayName("Should query low stock items")
    void shouldQueryLowStockItems() {
        when(itemRepository.findLowStockItems(tenantId)).thenReturn(List.of());

        List<InventoryItem> result = queryService.handle(new GetLowStockItemsQuery(tenantId));

        assertThat(result).isEmpty();
        verify(itemRepository).findLowStockItems(tenantId);
    }

    @Test
    @DisplayName("Should query valuation")
    void shouldQueryValuation() {
        Money valuation = Money.of(new BigDecimal("12500.50"), Currency.PEN);
        when(batchRepository.calculateTotalValuationByTenantId(tenantId)).thenReturn(valuation);

        Money result = queryService.handle(new GetInventoryValuationQuery(tenantId));

        assertThat(result).isEqualTo(valuation);
        verify(batchRepository).calculateTotalValuationByTenantId(tenantId);
    }
}
