package com.andeva.atelier.platform.inventory.domain.model.aggregates;

import com.andeva.atelier.platform.inventory.domain.exceptions.InsufficientStockException;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.enums.InventoryItemStatus;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.events.InventoryBatchAddedEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.InventoryItemCreatedEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.InventoryItemDeactivatedEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.LowStockThresholdReachedEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.StockAllocatedFifoEvent;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InventoryItem Aggregate Domain Tests")
class InventoryItemTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final Sku sku = Sku.of("BRK-PAD-001");
    private final String name = "Pastillas de Freno Delanteras Bosch";
    private final ItemCategory category = ItemCategory.BRAKES;
    private final Money basePrice = Money.of(new BigDecimal("120.00"), Currency.PEN);
    private final Quantity minStock = Quantity.of(new BigDecimal("5.00"));
    private final String unitOfMeasure = "SET";

    private InventoryItem createDefaultItem() {
        return InventoryItem.create(
                tenantId,
                name,
                sku,
                category,
                basePrice,
                minStock,
                unitOfMeasure
        );
    }

    private InventoryBatch createBatch(InventoryItem item, String batchNumber, BigDecimal qty, BigDecimal unitCost) {
        return InventoryBatch.create(
                tenantId,
                item.getId(),
                null,
                null,
                batchNumber,
                Quantity.of(qty),
                Money.of(unitCost, Currency.PEN),
                Instant.now(),
                null
        );
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create InventoryItem with initial zero stock and ACTIVE status")
        void shouldCreateItemWithZeroStock() {
            InventoryItem item = createDefaultItem();

            assertThat(item.getId()).isNotNull();
            assertThat(item.getTenantId()).isEqualTo(tenantId);
            assertThat(item.getName()).isEqualTo(name);
            assertThat(item.getSku()).isEqualTo(sku);
            assertThat(item.getCategory()).isEqualTo(category);
            assertThat(item.getBasePrice()).isEqualTo(basePrice);
            assertThat(item.getMinimumStock()).isEqualTo(minStock);
            assertThat(item.getTotalStock().value()).isEqualByComparingTo("0.00");
            assertThat(item.getStatus()).isEqualTo(InventoryItemStatus.ACTIVE);
            assertThat(item.getBatches()).isEmpty();

            assertThat(item.domainEvents()).anyMatch(e -> e instanceof InventoryItemCreatedEvent);
        }
    }

    @Nested
    @DisplayName("Batch Addition and Stock Accumulation")
    class BatchAdditionTests {

        @Test
        @DisplayName("Should add batch, increase total stock, and emit InventoryBatchAddedEvent")
        void shouldAddBatchSuccessfully() {
            InventoryItem item = createDefaultItem();
            InventoryBatch batch = createBatch(item, "BATCH-2026-001", new BigDecimal("20.00"), new BigDecimal("75.00"));

            item.addBatch(batch);

            assertThat(item.getTotalStock().value()).isEqualByComparingTo("20.00");
            assertThat(item.getBatches()).hasSize(1);
            assertThat(item.domainEvents()).anyMatch(e -> e instanceof InventoryBatchAddedEvent);
        }
    }

    @Nested
    @DisplayName("FIFO Stock Allocation and Low Stock Triggers")
    class FifoAllocationTests {

        @Test
        @DisplayName("Should allocate stock via FIFO, deduct remaining batch quantity, and trigger LowStockThresholdReachedEvent")
        void shouldAllocateStockAndTriggerLowStock() {
            InventoryItem item = createDefaultItem();
            // Minimum stock is 5.00. Add batch of 8.00.
            InventoryBatch batch = createBatch(item, "B1", new BigDecimal("8.00"), new BigDecimal("50.00"));
            item.addBatch(batch);

            item.clearDomainEvents();

            // Allocate 4.00 units -> remaining stock becomes 4.00, which is <= minStock (5.00)
            StockAllocation allocation = item.allocateStockFifo(Quantity.of(new BigDecimal("4.00")));

            assertThat(allocation.allocatedQuantity().value()).isEqualByComparingTo("4.00");
            assertThat(item.getTotalStock().value()).isEqualByComparingTo("4.00");
            assertThat(item.isLowStock()).isTrue();

            assertThat(item.domainEvents()).anyMatch(e -> e instanceof StockAllocatedFifoEvent);
            assertThat(item.domainEvents()).anyMatch(e -> e instanceof LowStockThresholdReachedEvent);
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when allocating more than available stock")
        void shouldThrowOnInsufficientStock() {
            InventoryItem item = createDefaultItem();
            InventoryBatch batch = createBatch(item, "B1", new BigDecimal("2.00"), new BigDecimal("50.00"));
            item.addBatch(batch);

            assertThatThrownBy(() -> item.allocateStockFifo(Quantity.of(new BigDecimal("5.00"))))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("Stock Restoration and Deactivation")
    class RestorationAndDeactivationTests {

        @Test
        @DisplayName("Should restore allocated stock to target batch and adjust quantities")
        void shouldReleaseStockAllocation() {
            InventoryItem item = createDefaultItem();
            InventoryBatch batch = createBatch(item, "B1", new BigDecimal("10.00"), new BigDecimal("50.00"));
            item.addBatch(batch);

            StockAllocation allocation = item.allocateStockFifo(Quantity.of(new BigDecimal("6.00")));
            assertThat(item.getTotalStock().value()).isEqualByComparingTo("4.00");

            item.clearDomainEvents();

            item.releaseStockAllocation(allocation);

            // Allocation restored, item remaining stock restored
            assertThat(batch.getRemainingQuantity().value()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("Should deactivate item and emit InventoryItemDeactivatedEvent")
        void shouldDeactivateItem() {
            InventoryItem item = createDefaultItem();
            item.deactivate();

            assertThat(item.getStatus()).isEqualTo(InventoryItemStatus.INACTIVE);
            assertThat(item.domainEvents()).anyMatch(e -> e instanceof InventoryItemDeactivatedEvent);
        }
    }
}
