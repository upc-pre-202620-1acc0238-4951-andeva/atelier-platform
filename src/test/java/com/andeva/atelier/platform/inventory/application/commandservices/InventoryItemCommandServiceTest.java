package com.andeva.atelier.platform.inventory.application.commandservices;

import com.andeva.atelier.platform.inventory.application.internal.commandservices.InventoryItemCommandServiceImpl;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.commands.AddInventoryBatchCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.AllocateStockFifoCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryItem Command Service Orchestration Tests")
class InventoryItemCommandServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;

    private InventoryItemCommandServiceImpl commandService;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final Sku sku = Sku.of("OIL-FLT-10W40");

    @BeforeEach
    void setUp() {
        commandService = new InventoryItemCommandServiceImpl(itemRepository);
    }

    @Test
    @DisplayName("Should successfully create inventory item when SKU is unique")
    void shouldCreateItemWhenSkuIsUnique() {
        CreateInventoryItemCommand command = new CreateInventoryItemCommand(
                tenantId,
                "Filtro de Aceite Castrol",
                sku,
                ItemCategory.FILTERS,
                Money.of(new BigDecimal("35.00"), Currency.PEN),
                Quantity.of(new BigDecimal("10.00")),
                "UNIT"
        );

        when(itemRepository.existsByTenantIdAndSku(tenantId, sku)).thenReturn(false);
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<InventoryItem, ApplicationError> result = commandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        InventoryItem item = result.getOrThrow();
        assertThat(item.getName()).isEqualTo("Filtro de Aceite Castrol");
        assertThat(item.getSku()).isEqualTo(sku);
        verify(itemRepository).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Should return conflict ApplicationError when SKU already exists for tenant")
    void shouldReturnConflictWhenSkuExists() {
        CreateInventoryItemCommand command = new CreateInventoryItemCommand(
                tenantId,
                "Filtro de Aceite Castrol",
                sku,
                ItemCategory.FILTERS,
                Money.of(new BigDecimal("35.00"), Currency.PEN),
                Quantity.of(new BigDecimal("10.00")),
                "UNIT"
        );

        when(itemRepository.existsByTenantIdAndSku(tenantId, sku)).thenReturn(true);

        Result<InventoryItem, ApplicationError> result = commandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should add batch to existing inventory item")
    void shouldAddBatchToExistingItem() {
        InventoryItem item = InventoryItem.create(
                tenantId, "Filtro", sku, ItemCategory.FILTERS,
                Money.of(new BigDecimal("30.00"), Currency.PEN),
                Quantity.of(new BigDecimal("5.00")), "UNIT"
        );

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddInventoryBatchCommand command = new AddInventoryBatchCommand(
                item.getId(),
                null,
                null,
                "LOT-001",
                Quantity.of(new BigDecimal("15.00")),
                Money.of(new BigDecimal("20.00"), Currency.PEN),
                Instant.now(),
                null
        );

        Result<InventoryBatch, ApplicationError> result = commandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(item.getTotalStock().value()).isEqualByComparingTo("15.00");
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Should allocate stock via FIFO and persist updated item")
    void shouldAllocateStockFifo() {
        InventoryItem item = InventoryItem.create(
                tenantId, "Filtro", sku, ItemCategory.FILTERS,
                Money.of(new BigDecimal("30.00"), Currency.PEN),
                Quantity.of(new BigDecimal("5.00")), "UNIT"
        );
        item.addBatch(InventoryBatch.create(tenantId, item.getId(), null, null, "LOT-001",
                Quantity.of(new BigDecimal("10.00")), Money.of(new BigDecimal("20.00"), Currency.PEN), Instant.now(), null));

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocateStockFifoCommand command = new AllocateStockFifoCommand(
                item.getId(),
                Quantity.of(new BigDecimal("4.00")),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        Result<StockAllocation, ApplicationError> result = commandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrThrow().allocatedQuantity().value()).isEqualByComparingTo("4.00");
        assertThat(item.getTotalStock().value()).isEqualByComparingTo("6.00");
        verify(itemRepository).save(item);
    }
}
