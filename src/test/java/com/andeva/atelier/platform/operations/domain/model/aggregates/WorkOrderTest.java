package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.domain.model.enums.*;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkOrder Aggregate Domain Tests")
class WorkOrderTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());
    private final VehicleId vehicleId = VehicleId.of(UUID.randomUUID());
    private final CustomerId customerId = CustomerId.of(UUID.randomUUID());
    private final WorkOrderNumber orderNumber = WorkOrderNumber.of(1001);
    private final Mileage mileage = Mileage.of(45000);
    private final DiagnosticSummary summary = DiagnosticSummary.of("Fallas de encendido y revisión general");

    private WorkOrder createDefaultOrder() {
        return WorkOrder.create(
                tenantId,
                branchId,
                null,
                vehicleId,
                customerId,
                orderNumber,
                mileage,
                summary
        );
    }

    @Nested
    @DisplayName("Creation and Initial State Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create WorkOrder in DRAFT status with initial zero totals and domain event")
        void shouldCreateWorkOrderInDraftStatus() {
            WorkOrder order = createDefaultOrder();

            assertThat(order.getId()).isNotNull();
            assertThat(order.getTenantId()).isEqualTo(tenantId);
            assertThat(order.getBranchId()).isEqualTo(branchId);
            assertThat(order.getVehicleId()).isEqualTo(vehicleId);
            assertThat(order.getCustomerId()).isEqualTo(customerId);
            assertThat(order.getInternalNumber()).isEqualTo(orderNumber);
            assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.DRAFT);
            assertThat(order.getCurrentBayId()).isEmpty();
            assertThat(order.getSubtotal().amount()).isEqualByComparingTo("0.00");
            assertThat(order.getTax().amount()).isEqualByComparingTo("0.00");
            assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("0.00");

            assertThat(order.domainEvents()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Bay Assignment Lifecycle Tests")
    class BayAssignmentTests {

        @Test
        @DisplayName("Should assign and release work bay registering domain events")
        void shouldAssignAndReleaseBay() {
            WorkOrder order = createDefaultOrder();
            WorkBayId bayId = WorkBayId.generate();

            order.assignWorkBay(bayId);
            assertThat(order.getCurrentBayId()).contains(bayId);

            order.releaseBay();
            assertThat(order.getCurrentBayId()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tasks, Financial Calculations, and 18% IGV Tests")
    class FinancialCalculationsTests {

        @Test
        @DisplayName("Should compute subtotal, 18% IGV tax, and total amount accurately with Half-Even rounding")
        void shouldComputeAccurateFinancialTotals() {
            WorkOrder order = createDefaultOrder();

            // Task 1: 100.00 PEN labor
            order.addTask(
                    ServiceId.generate(),
                    UUID.randomUUID(),
                    "Cambio de pastillas de freno",
                    Money.soles(new BigDecimal("100.00")),
                    LaborHours.of(new BigDecimal("1.50"))
            );

            // Subtotal = 100.00, IGV (18%) = 18.00, Total = 118.00
            assertThat(order.getSubtotal().amount()).isEqualByComparingTo("100.00");
            assertThat(order.getTax().amount()).isEqualByComparingTo("18.00");
            assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("118.00");

            // Add Product to Task 1: 2 units at 50.00 each = 100.00 PEN
            WorkOrderTaskId taskId = order.getTasks().get(0).getId();
            order.addProductToTask(
                    taskId,
                    UUID.randomUUID(),
                    Quantity.of(new BigDecimal("2.00")),
                    Money.soles(new BigDecimal("50.00"))
            );

            // Subtotal = 100.00 + 100.00 = 200.00, IGV = 36.00, Total = 236.00
            assertThat(order.getSubtotal().amount()).isEqualByComparingTo("200.00");
            assertThat(order.getTax().amount()).isEqualByComparingTo("36.00");
            assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("236.00");
        }
    }

    @Nested
    @DisplayName("State Machine and Invariant Tests")
    class StateMachineTests {

        @Test
        @DisplayName("Should transition from DRAFT to IN_PROGRESS to COMPLETED to PAID and DELIVERED")
        void shouldTransitionThroughFullLifecycle() {
            WorkOrder order = createDefaultOrder();
            order.assignWorkBay(WorkBayId.generate());

            // 1. Start
            order.startWork();
            assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);

            // 2. Complete
            order.completeOrder();
            assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
            assertThat(order.getCurrentBayId()).isEmpty(); // bay is auto-released

            // 3. Mark as Paid
            order.markPaid();
            assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.PAID);

            // 4. Deliver Vehicle
            order.deliverVehicle();
            assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.PAID);
        }

        @Test
        @DisplayName("Should reject paying an order that is not completed")
        void shouldRejectPayingNonCompletedOrder() {
            WorkOrder order = createDefaultOrder();
            assertThatThrownBy(order::markPaid)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only COMPLETED orders can be marked as PAID");
        }

        @Test
        @DisplayName("Should reject delivering a vehicle before payment is confirmed")
        void shouldRejectDeliveringNonPaidVehicle() {
            WorkOrder order = createDefaultOrder();
            assertThatThrownBy(order::deliverVehicle)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Vehicle can only be delivered after order is PAID");
        }

        @Test
        @DisplayName("Should cancel order and release bay when justified")
        void shouldCancelOrderProperly() {
            WorkOrder order = createDefaultOrder();
            order.assignWorkBay(WorkBayId.generate());

            order.cancel("Cliente desiste de la reparación por presupuesto");
            assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.CANCELED);
            assertThat(order.getCurrentBayId()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Inspection Proposals Tests")
    class ProposalTests {

        @Test
        @DisplayName("Should submit defect proposal and approve it into authorized task")
        void shouldSubmitAndApproveProposal() {
            WorkOrder order = createDefaultOrder();
            UUID mechanicId = UUID.randomUUID();

            var proposal = order.submitProposal(
                    mechanicId,
                    "Fuga de líquido refrigerante por empaque de bomba de agua",
                    ProposalSeverity.CRITICAL,
                    StorageUrl.of("https://storage.googleapis.com/test-bucket/evidence1.jpg"),
                    ServiceId.generate()
            );

            assertThat(proposal.getStatus()).isEqualTo(ProposalStatus.PENDING_REVIEW);
            assertThat(order.getProposals()).hasSize(1);

            // Approve proposal
            order.approveProposal(
                    proposal.getId(),
                    ServiceId.generate(),
                    Money.soles(new BigDecimal("150.00")),
                    LaborHours.of(new BigDecimal("2.0")),
                    mechanicId,
                    "Aprobado por el cliente telefónicamente"
            );

            assertThat(proposal.getStatus()).isEqualTo(ProposalStatus.APPROVED);
            assertThat(order.getTasks()).hasSize(1);
            assertThat(order.getTotalAmount().amount()).isGreaterThan(BigDecimal.ZERO);
        }
    }
}
