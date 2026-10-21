package com.andeva.atelier.platform.inventory.application.commandservices;

import com.andeva.atelier.platform.inventory.application.internal.commandservices.InventoryItemCommandServiceImpl;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.commands.AllocateStockFifoCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReturnWorkOrderPartsCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.entities.WorkOrderPartAllocation;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.inventory.domain.repositories.WorkOrderPartAllocationRepository;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Parts charged to a work order go back to their batch, with an audit trail (US33, US35)")
class WorkOrderPartReturnsTest {

    @Mock
    private InventoryItemRepository itemRepository;

    private final Map<UUID, WorkOrderPartAllocation> allocations = new LinkedHashMap<>();
    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final UUID workOrderId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private InventoryItemCommandServiceImpl service;
    private InventoryItem item;
    private InventoryBatch oldBatch;
    private InventoryBatch newBatch;

    @BeforeEach
    void setUp() {
        WorkOrderPartAllocationRepository allocationRepository = new WorkOrderPartAllocationRepository() {
            @Override
            public WorkOrderPartAllocation save(WorkOrderPartAllocation allocation) {
                allocations.put(allocation.getId(), allocation);
                return allocation;
            }

            @Override
            public List<WorkOrderPartAllocation> findOpenByWorkOrderTaskAndItem(UUID orderId, UUID task, InventoryItemId itemId) {
                return allocations.values().stream()
                        .filter(a -> a.getWorkOrderId().equals(orderId) && a.getTaskId().equals(task) && a.isOpen())
                        .sorted(Comparator.comparing(WorkOrderPartAllocation::getAllocatedAt).reversed())
                        .toList();
            }

            @Override
            public List<WorkOrderPartAllocation> findByWorkOrderId(UUID orderId) {
                return new ArrayList<>(allocations.values());
            }
        };
        service = new InventoryItemCommandServiceImpl(itemRepository, allocationRepository);

        item = InventoryItem.create(tenantId, "Bujía NGK", Sku.of("SPK-NGK-01"), ItemCategory.ELECTRICAL,
                Money.of(new BigDecimal("12.00"), Currency.PEN), Quantity.of(new BigDecimal("1.00")), "UNIT");
        oldBatch = InventoryBatch.create(tenantId, item.getId(), null, null, "L-OLD", Quantity.of(new BigDecimal("2.00")),
                Money.of(new BigDecimal("5.00"), Currency.PEN), Instant.now().minusSeconds(86_400), null);
        newBatch = InventoryBatch.create(tenantId, item.getId(), null, null, "L-NEW", Quantity.of(new BigDecimal("10.00")),
                Money.of(new BigDecimal("7.00"), Currency.PEN), Instant.now(), null);
        item.addBatch(oldBatch);
        item.addBatch(newBatch);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("A multi-batch charge records each deduction with its cost (2 × S/5 + 1 × S/7 = S/17)")
    void shouldRecordAllocationsPerBatch() {
        service.handle(new AllocateStockFifoCommand(item.getId(), Quantity.of(new BigDecimal("3.00")), workOrderId, taskId));

        assertThat(allocations.values()).hasSize(2);
        BigDecimal cost = allocations.values().stream().map(WorkOrderPartAllocation::costOfRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(cost).isEqualByComparingTo("17.00");
    }

    @Test
    @DisplayName("Withdrawn units return to the batches they came from and keep who returned them")
    void shouldReturnUnitsToOriginBatches() {
        service.handle(new AllocateStockFifoCommand(item.getId(), Quantity.of(new BigDecimal("3.00")), workOrderId, taskId));
        assertThat(oldBatch.getRemainingQuantity().value()).isEqualByComparingTo("0.00");
        UUID admin = UUID.randomUUID();

        var result = service.handle(new ReturnWorkOrderPartsCommand(item.getId(), workOrderId, taskId,
                Quantity.of(new BigDecimal("3.00")), admin, "Pieza no corresponde"));

        assertThat(result.getOrThrow()).isEqualByComparingTo("3.00");
        assertThat(oldBatch.getRemainingQuantity().value()).isEqualByComparingTo("2.00");
        assertThat(newBatch.getRemainingQuantity().value()).isEqualByComparingTo("10.00");
        assertThat(item.getTotalStock().value()).isEqualByComparingTo("12.00");
        assertThat(allocations.values()).allSatisfy(a -> {
            assertThat(a.isOpen()).isFalse();
            assertThat(a.getLastReturnedBy()).isEqualTo(admin);
            assertThat(a.getLastReturnedAt()).isNotNull();
        });
    }
}
