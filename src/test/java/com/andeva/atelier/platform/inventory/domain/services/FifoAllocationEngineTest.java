package com.andeva.atelier.platform.inventory.domain.services;

import com.andeva.atelier.platform.inventory.domain.exceptions.InsufficientStockException;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FifoAllocationEngine Domain Service Tests")
class FifoAllocationEngineTest {

    private FifoAllocationEngine engine;
    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final InventoryItemId itemId = InventoryItemId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        engine = new FifoAllocationEngine();
    }

    private InventoryBatch createBatch(String batchNumber, BigDecimal qty, BigDecimal unitCost, Instant arrivalDate) {
        return InventoryBatch.create(
                tenantId,
                itemId,
                null,
                null,
                batchNumber,
                Quantity.of(qty),
                Money.of(unitCost, Currency.PEN),
                arrivalDate,
                null
        );
    }

    @Nested
    @DisplayName("Single Batch Deductions")
    class SingleBatchTests {

        @Test
        @DisplayName("Should fully satisfy request from a single batch when available quantity is sufficient")
        void shouldAllocateFromSingleBatch() {
            Instant now = Instant.now();
            InventoryBatch batch = createBatch("BATCH-001", new BigDecimal("10.00"), new BigDecimal("50.00"), now);
            List<InventoryBatch> batches = new ArrayList<>(List.of(batch));

            StockAllocation allocation = engine.allocate(batches, Quantity.of(new BigDecimal("4.00")));

            assertThat(allocation).isNotNull();
            assertThat(allocation.allocatedQuantity().value()).isEqualByComparingTo("4.00");
            assertThat(allocation.totalCostOfGoodsSold().amount()).isEqualByComparingTo("200.00");
            assertThat(allocation.deductions()).hasSize(1);
            assertThat(allocation.deductions().get(0).quantityDeducted().value()).isEqualByComparingTo("4.00");
            assertThat(batch.getRemainingQuantity().value()).isEqualByComparingTo("6.00");
        }

        @Test
        @DisplayName("Should completely deplete single batch when requested quantity equals batch remaining quantity")
        void shouldDepleteSingleBatchExact() {
            Instant now = Instant.now();
            InventoryBatch batch = createBatch("BATCH-001", new BigDecimal("5.00"), new BigDecimal("30.00"), now);
            List<InventoryBatch> batches = new ArrayList<>(List.of(batch));

            StockAllocation allocation = engine.allocate(batches, Quantity.of(new BigDecimal("5.00")));

            assertThat(allocation.allocatedQuantity().value()).isEqualByComparingTo("5.00");
            assertThat(allocation.totalCostOfGoodsSold().amount()).isEqualByComparingTo("150.00");
            assertThat(batch.getRemainingQuantity().value()).isEqualByComparingTo("0.00");
            assertThat(batch.isDepleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Multi-Batch FIFO Chronological Deductions")
    class MultiBatchFifoTests {

        @Test
        @DisplayName("Should allocate strictly in chronological order by arrival date across multiple batches")
        void shouldAllocateStrictlyByArrivalDate() {
            Instant t1 = Instant.now().minus(5, ChronoUnit.DAYS);
            Instant t2 = Instant.now().minus(2, ChronoUnit.DAYS);
            Instant t3 = Instant.now();

            InventoryBatch batchOldest = createBatch("BATCH-OLD", new BigDecimal("3.00"), new BigDecimal("10.00"), t1);
            InventoryBatch batchMiddle = createBatch("BATCH-MID", new BigDecimal("4.00"), new BigDecimal("15.00"), t2);
            InventoryBatch batchNewest = createBatch("BATCH-NEW", new BigDecimal("10.00"), new BigDecimal("20.00"), t3);

            // Pass in arbitrary order to verify the engine sorts them strictly by arrivalDate
            List<InventoryBatch> batches = new ArrayList<>(List.of(batchNewest, batchOldest, batchMiddle));

            // Request 6 units: should take 3 from batchOldest, and 3 from batchMiddle
            StockAllocation allocation = engine.allocate(batches, Quantity.of(new BigDecimal("6.00")));

            assertThat(allocation.allocatedQuantity().value()).isEqualByComparingTo("6.00");
            // COGS: 3 * 10.00 + 3 * 15.00 = 30.00 + 45.00 = 75.00
            assertThat(allocation.totalCostOfGoodsSold().amount()).isEqualByComparingTo("75.00");
            assertThat(allocation.deductions()).hasSize(2);

            assertThat(batchOldest.getRemainingQuantity().value()).isEqualByComparingTo("0.00");
            assertThat(batchOldest.isDepleted()).isTrue();

            assertThat(batchMiddle.getRemainingQuantity().value()).isEqualByComparingTo("1.00");
            assertThat(batchMiddle.isDepleted()).isFalse();

            assertThat(batchNewest.getRemainingQuantity().value()).isEqualByComparingTo("10.00");
            assertThat(batchNewest.isDepleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Insufficient Stock and Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Should throw InsufficientStockException when available stock is less than requested")
        void shouldThrowWhenStockInsufficient() {
            Instant now = Instant.now();
            InventoryBatch batch = createBatch("BATCH-001", new BigDecimal("3.00"), new BigDecimal("10.00"), now);
            List<InventoryBatch> batches = new ArrayList<>(List.of(batch));

            assertThatThrownBy(() -> engine.allocate(batches, Quantity.of(new BigDecimal("5.00"))))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when batch list is empty")
        void shouldThrowWhenNoBatches() {
            List<InventoryBatch> batches = new ArrayList<>();

            assertThatThrownBy(() -> engine.allocate(batches, Quantity.of(new BigDecimal("1.00"))))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }
}
