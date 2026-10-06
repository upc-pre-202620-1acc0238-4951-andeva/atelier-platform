package com.andeva.atelier.platform.inventory.interfaces.rest.controllers;

import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.application.commandservices.PurchaseOrderCommandService;
import com.andeva.atelier.platform.inventory.application.commandservices.SupplierCommandService;
import com.andeva.atelier.platform.inventory.application.queryservices.InventoryItemQueryService;
import com.andeva.atelier.platform.inventory.application.queryservices.PurchaseOrderQueryService;
import com.andeva.atelier.platform.inventory.application.queryservices.SupplierQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.RegisterSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryBatchesByItemIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemsByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryValuationQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetLowStockItemsQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrderDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrdersByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSupplierByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSuppliersByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetInventoryItemBySkuQuery;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.CreatePartResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.RegisterSupplierResource;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory REST Controllers Unit Tests")
class InventoryControllersTest {

    @Mock
    private InventoryItemCommandService inventoryItemCommandService;
    @Mock
    private InventoryItemQueryService inventoryItemQueryService;

    @Mock
    private SupplierCommandService supplierCommandService;
    @Mock
    private SupplierQueryService supplierQueryService;

    @Mock
    private PurchaseOrderCommandService purchaseOrderCommandService;
    @Mock
    private PurchaseOrderQueryService purchaseOrderQueryService;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());

    @Nested
    @DisplayName("PartsCatalogController Tests")
    class PartsCatalogControllerTests {

        @Test
        @DisplayName("Should return 200 OK with list of parts")
        void shouldReturnParts() {
            PartsCatalogController controller = new PartsCatalogController(
                    inventoryItemCommandService, inventoryItemQueryService
            );

            InventoryItem item = InventoryItem.create(
                    tenantId, "Filtro Aceite", Sku.of("FLT-001"), ItemCategory.FILTERS,
                    Money.of(new BigDecimal("35.00"), Currency.PEN),
                    Quantity.of(new BigDecimal("10.00")), "UNIT"
            );

            when(inventoryItemQueryService.handle(any(GetInventoryItemsByTenantIdQuery.class)))
                    .thenReturn(List.of(item));

            ResponseEntity<?> response = controller.getParts(null, tenantId.value(), null, null, null, false, 0, 20);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("Should return 201 Created when creating new part")
        void shouldCreatePart() {
            PartsCatalogController controller = new PartsCatalogController(
                    inventoryItemCommandService, inventoryItemQueryService
            );

            InventoryItem item = InventoryItem.create(
                    tenantId, "Pastillas Freno", Sku.of("BRK-001"), ItemCategory.BRAKES,
                    Money.of(new BigDecimal("120.00"), Currency.PEN),
                    Quantity.of(new BigDecimal("5.00")), "SET"
            );

            when(inventoryItemCommandService.handle(any(CreateInventoryItemCommand.class)))
                    .thenReturn(Result.success(item));

            CreatePartResource resource = new CreatePartResource(
                    "Pastillas Freno",
                    "BRK-001",
                    ItemCategory.BRAKES,
                    new BigDecimal("120.00"),
                    new BigDecimal("5.00"),
                    "SET"
            );

            ResponseEntity<?> response = controller.createPart(null, tenantId.value(), resource, UriComponentsBuilder.newInstance());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("Should return 200 OK with detail when part exists")
        void shouldGetPartById() {
            PartsCatalogController controller = new PartsCatalogController(
                    inventoryItemCommandService, inventoryItemQueryService
            );

            InventoryItem item = InventoryItem.create(
                    tenantId, "Pastillas Freno", Sku.of("BRK-001"), ItemCategory.BRAKES,
                    Money.of(new BigDecimal("120.00"), Currency.PEN),
                    Quantity.of(new BigDecimal("5.00")), "SET"
            );

            when(inventoryItemQueryService.handle(any(GetInventoryItemDetailQuery.class)))
                    .thenReturn(Optional.of(item));

            ResponseEntity<?> response = controller.getPartById(item.getId().value());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return 200 OK when getting part by SKU")
        void shouldGetPartBySku() {
            PartsCatalogController controller = new PartsCatalogController(
                    inventoryItemCommandService, inventoryItemQueryService
            );

            InventoryItem item = InventoryItem.create(
                    tenantId, "Pastillas Freno", Sku.of("BRK-001"), ItemCategory.BRAKES,
                    Money.of(new BigDecimal("120.00"), Currency.PEN),
                    Quantity.of(new BigDecimal("5.00")), "SET"
            );

            when(inventoryItemQueryService.handle(any(GetInventoryItemBySkuQuery.class)))
                    .thenReturn(Optional.of(item));

            ResponseEntity<?> response = controller.getPartBySku(null, tenantId.value(), "BRK-001");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return 200 OK when getting parts by category")
        void shouldGetPartsByCategory() {
            PartsCatalogController controller = new PartsCatalogController(
                    inventoryItemCommandService, inventoryItemQueryService
            );

            InventoryItem item = InventoryItem.create(
                    tenantId, "Pastillas Freno", Sku.of("BRK-001"), ItemCategory.BRAKES,
                    Money.of(new BigDecimal("120.00"), Currency.PEN),
                    Quantity.of(new BigDecimal("5.00")), "SET"
            );

            when(inventoryItemQueryService.handle(any(GetInventoryItemsByTenantIdQuery.class)))
                    .thenReturn(List.of(item));

            ResponseEntity<?> response = controller.getPartsByCategory(null, tenantId.value(), "BRAKES");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("SuppliersController Tests")
    class SuppliersControllerTests {

        @Test
        @DisplayName("Should return list of suppliers")
        void shouldReturnSuppliers() {
            SuppliersController controller = new SuppliersController(supplierCommandService, supplierQueryService);

            Supplier supplier = Supplier.register(
                    tenantId, "Distribuidora Lima SAC", TaxId.of("20123456789"),
                    "Juan Pérez", "+51999888777", "juan@lima.pe", "Av. Perú 123"
            );

            when(supplierQueryService.handle(any(GetSuppliersByTenantIdQuery.class)))
                    .thenReturn(List.of(supplier));

            ResponseEntity<?> response = controller.getSuppliers(null, tenantId.value(), null, null);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("Should register supplier and return 201 Created")
        void shouldRegisterSupplier() {
            SuppliersController controller = new SuppliersController(supplierCommandService, supplierQueryService);

            Supplier supplier = Supplier.register(
                    tenantId, "Distribuidora Lima SAC", TaxId.of("20123456789"),
                    "Juan Pérez", "+51999888777", "juan@lima.pe", "Av. Perú 123"
            );

            when(supplierCommandService.handle(any(RegisterSupplierCommand.class)))
                    .thenReturn(Result.success(supplier));

            RegisterSupplierResource resource = new RegisterSupplierResource(
                    "Distribuidora Lima SAC",
                    "20123456789",
                    "Juan Pérez",
                    "+51999888777",
                    "juan@lima.pe",
                    "Av. Perú 123"
            );

            ResponseEntity<?> response = controller.registerSupplier(null, tenantId.value(), resource, UriComponentsBuilder.newInstance());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("PurchaseOrdersController Tests")
    class PurchaseOrdersControllerTests {

        @Test
        @DisplayName("Should return purchase orders for tenant")
        void shouldReturnPurchaseOrders() {
            PurchaseOrdersController controller = new PurchaseOrdersController(purchaseOrderCommandService, purchaseOrderQueryService);

            PurchaseOrder order = PurchaseOrder.create(
                    tenantId, SupplierId.of(UUID.randomUUID()), BranchId.of(UUID.randomUUID()),
                    PurchaseOrderNumber.of("PO-001")
            );

            when(purchaseOrderQueryService.handle(any(GetPurchaseOrdersByTenantIdQuery.class)))
                    .thenReturn(List.of(order));

            ResponseEntity<?> response = controller.getPurchaseOrders(null, tenantId.value(), null, null);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("Should get purchase order by id")
        void shouldGetPurchaseOrderById() {
            PurchaseOrdersController controller = new PurchaseOrdersController(purchaseOrderCommandService, purchaseOrderQueryService);

            PurchaseOrder order = PurchaseOrder.create(
                    tenantId, SupplierId.of(UUID.randomUUID()), BranchId.of(UUID.randomUUID()),
                    PurchaseOrderNumber.of("PO-001")
            );

            when(purchaseOrderQueryService.handle(any(GetPurchaseOrderDetailQuery.class)))
                    .thenReturn(Optional.of(order));

            ResponseEntity<?> response = controller.getPurchaseOrderById(order.getId().value());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("BatchesController Tests")
    class BatchesControllerTests {

        @Test
        @DisplayName("Should return batches for an item")
        void shouldReturnBatchesByPart() {
            BatchesController controller = new BatchesController(inventoryItemCommandService, inventoryItemQueryService);

            InventoryBatch batch = InventoryBatch.create(
                    tenantId,
                    com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId.of(UUID.randomUUID()),
                    null, null, "BATCH-1", Quantity.of(new BigDecimal("10.00")),
                    Money.of(new BigDecimal("40.00"), Currency.PEN), Instant.now(), null
            );

            when(inventoryItemQueryService.handle(any(GetInventoryBatchesByItemIdQuery.class)))
                    .thenReturn(List.of(batch));

            ResponseEntity<?> response = controller.getBatchesByPart(batch.getItemId().value());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(List.class);
        }
    }

    @Nested
    @DisplayName("StockAlertsController Tests")
    class StockAlertsControllerTests {

        @Test
        @DisplayName("Should return low stock alerts")
        void shouldReturnLowStockAlerts() {
            StockAlertsController controller = new StockAlertsController(inventoryItemQueryService);

            InventoryItem item = InventoryItem.create(
                    tenantId, "Filtro", Sku.of("FLT-1"), ItemCategory.FILTERS,
                    Money.of(new BigDecimal("30.00"), Currency.PEN),
                    Quantity.of(new BigDecimal("10.00")), "UNIT"
            );

            when(inventoryItemQueryService.handle(any(GetLowStockItemsQuery.class)))
                    .thenReturn(List.of(item));

            ResponseEntity<?> response = controller.getLowStockAlerts(null, tenantId.value());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("Should evaluate stock thresholds and return evaluation summary")
        void shouldEvaluateThresholds() {
            StockAlertsController controller = new StockAlertsController(inventoryItemQueryService);

            when(inventoryItemQueryService.handle(any(GetInventoryItemsByTenantIdQuery.class)))
                    .thenReturn(List.of());
            when(inventoryItemQueryService.handle(any(GetLowStockItemsQuery.class)))
                    .thenReturn(List.of());
            when(inventoryItemQueryService.handle(any(GetInventoryValuationQuery.class)))
                    .thenReturn(Money.of(BigDecimal.ZERO, Currency.PEN));

            ResponseEntity<?> response = controller.evaluateStockThresholds(null, tenantId.value());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
