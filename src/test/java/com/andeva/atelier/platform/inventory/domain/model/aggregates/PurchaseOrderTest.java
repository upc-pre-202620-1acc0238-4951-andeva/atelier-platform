package com.andeva.atelier.platform.inventory.domain.model.aggregates;

import com.andeva.atelier.platform.inventory.domain.exceptions.InvalidPurchaseOrderTransitionException;
import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import com.andeva.atelier.platform.inventory.domain.model.events.PurchaseOrderCanceledEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.PurchaseOrderCreatedEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.PurchaseOrderReceivedEvent;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchaseOrder Aggregate Domain Tests")
class PurchaseOrderTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final SupplierId supplierId = SupplierId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());
    private final PurchaseOrderNumber orderNumber = PurchaseOrderNumber.of("PO-2026-0001");

    private PurchaseOrder createDefaultOrder() {
        return PurchaseOrder.create(
                tenantId,
                supplierId,
                branchId,
                orderNumber
        );
    }

    @Nested
    @DisplayName("Creation and Item Addition")
    class CreationAndItemsTests {

        @Test
        @DisplayName("Should create PurchaseOrder in DRAFT status with initial zero cost and domain event")
        void shouldCreateInDraftStatus() {
            PurchaseOrder order = createDefaultOrder();

            assertThat(order.getId()).isNotNull();
            assertThat(order.getTenantId()).isEqualTo(tenantId);
            assertThat(order.getSupplierId()).isEqualTo(supplierId);
            assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
            assertThat(order.getTotalCost().amount()).isEqualByComparingTo("0.00");
            assertThat(order.getItems()).isEmpty();

            assertThat(order.domainEvents()).anyMatch(e -> e instanceof PurchaseOrderCreatedEvent);
        }

        @Test
        @DisplayName("Should add items and recalculate total cost correctly")
        void shouldAddItemsAndRecalculateTotal() {
            PurchaseOrder order = createDefaultOrder();

            InventoryItemId item1 = InventoryItemId.of(UUID.randomUUID());
            InventoryItemId item2 = InventoryItemId.of(UUID.randomUUID());

            // 10 units at 50.00 = 500.00
            order.addItem(item1, Quantity.of(new BigDecimal("10.00")), Money.of(new BigDecimal("50.00"), Currency.PEN));
            // 5 units at 20.00 = 100.00
            order.addItem(item2, Quantity.of(new BigDecimal("5.00")), Money.of(new BigDecimal("20.00"), Currency.PEN));

            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getTotalCost().amount()).isEqualByComparingTo("600.00");
        }
    }

    @Nested
    @DisplayName("Lifecycle Transitions: Issue, Receive, Cancel")
    class LifecycleTests {

        @Test
        @DisplayName("Should transition from DRAFT to ISSUED and then to RECEIVED")
        void shouldTransitionToIssuedAndReceived() {
            PurchaseOrder order = createDefaultOrder();
            order.addItem(InventoryItemId.of(UUID.randomUUID()), Quantity.of(new BigDecimal("5.00")),
                    Money.of(new BigDecimal("100.00"), Currency.PEN));

            order.issueOrder();
            assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.ISSUED);

            order.clearDomainEvents();

            StorageUrl receiptUrl = StorageUrl.of("https://storage.atelier.internal/receipts/inv-001.pdf");
            order.receiveOrder(receiptUrl, "FAC-001-9988", Instant.now());

            assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
            assertThat(order.getReceiptImageUrl()).contains(receiptUrl);
            assertThat(order.getReceiptNumber()).contains("FAC-001-9988");
            assertThat(order.getReceivedAt()).isPresent();

            assertThat(order.domainEvents()).anyMatch(e -> e instanceof PurchaseOrderReceivedEvent);
        }

        @Test
        @DisplayName("Should cancel purchase order in DRAFT or ISSUED state and emit PurchaseOrderCanceledEvent")
        void shouldCancelPurchaseOrder() {
            PurchaseOrder order = createDefaultOrder();
            order.cancelOrder("Proveedor sin stock disponible");

            assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELED);
            assertThat(order.domainEvents()).anyMatch(e -> e instanceof PurchaseOrderCanceledEvent);
        }

        @Test
        @DisplayName("Should throw InvalidPurchaseOrderStateException when cancelling already received order")
        void shouldThrowWhenCancellingReceivedOrder() {
            PurchaseOrder order = createDefaultOrder();
            order.addItem(InventoryItemId.of(UUID.randomUUID()), Quantity.of(new BigDecimal("2.00")),
                    Money.of(new BigDecimal("50.00"), Currency.PEN));
            order.issueOrder();
            order.receiveOrder(StorageUrl.of("https://storage.atelier.internal/rec.pdf"), "REC-01", Instant.now());

            assertThatThrownBy(() -> order.cancelOrder("Cancel invalid"))
                    .isInstanceOf(InvalidPurchaseOrderTransitionException.class);
        }
    }
}
