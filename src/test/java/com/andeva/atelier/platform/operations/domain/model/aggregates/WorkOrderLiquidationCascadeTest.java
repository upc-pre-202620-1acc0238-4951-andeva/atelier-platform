package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderTaskResource;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.WorkOrderTaskResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AT-23 / US36 - Automatic Cascade Economic Liquidation of WorkOrder")
class WorkOrderLiquidationCascadeTest {

    private WorkOrder newDraftOrder() {
        return WorkOrder.create(
                TenantId.generate(),
                BranchId.generate(),
                null,
                VehicleId.generate(),
                CustomerId.generate(),
                WorkOrderNumber.of(101),
                Mileage.of(42500),
                DiagnosticSummary.of("Preventive maintenance and brake inspection")
        );
    }

    @Test
    @DisplayName("Scenario 1: Calculates consolidated price per task, subtotal, 18% IGV and total automatically")
    void calculatesConsolidatedTaskPriceSubtotalIgvAndTotal() {
        WorkOrder order = newDraftOrder();

        // Task 1: Base labor S/ 150.00 + 2x Brake Pads at S/ 80.00 (S/ 160.00) => Consolidated S/ 310.00
        WorkOrderTask task1 = order.addTask(
                ServiceId.generate(),
                UUID.randomUUID(),
                "Front brake pad replacement",
                Money.soles(new BigDecimal("150.00")),
                LaborHours.of(new BigDecimal("1.50"))
        );
        UUID brakePadsId = UUID.randomUUID();
        order.addProductToTask(task1.getId(), brakePadsId, Quantity.of(new BigDecimal("2")), Money.soles(new BigDecimal("80.00")));

        // Task 2: Base labor S/ 90.00 + 1x Oil Filter at S/ 45.00 + 4L Oil at S/ 25.00 (S/ 145.00) => Consolidated S/ 235.00
        WorkOrderTask task2 = order.addTask(
                ServiceId.generate(),
                UUID.randomUUID(),
                "Synthetic oil change",
                Money.soles(new BigDecimal("90.00")),
                LaborHours.of(new BigDecimal("1.00"))
        );
        order.addProductToTask(task2.getId(), UUID.randomUUID(), Quantity.of(new BigDecimal("1")), Money.soles(new BigDecimal("45.00")));
        order.addProductToTask(task2.getId(), UUID.randomUUID(), Quantity.of(new BigDecimal("4")), Money.soles(new BigDecimal("25.00")));

        // Verify per-task consolidated prices
        assertThat(task1.getConsolidatedPrice().amount()).isEqualByComparingTo("310.00");
        assertThat(task2.getConsolidatedPrice().amount()).isEqualByComparingTo("235.00");

        // Verify cascade totals: Subtotal = 545.00, IGV (18%) = 98.10, Total = 643.10
        assertThat(order.getSubtotal().amount()).isEqualByComparingTo("545.00");
        assertThat(order.getTax().amount()).isEqualByComparingTo("98.10");
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("643.10");

        // Verify REST resource exposes both base price and consolidatedPrice
        WorkOrderTaskResource task1Resource = WorkOrderTaskResourceAssembler.toResourceFromEntity(
                task1, "Brake Service", "Juan Perez", id -> "Ceramic Brake Pads"
        );
        assertThat(task1Resource.price()).isEqualByComparingTo("150.00");
        assertThat(task1Resource.consolidatedPrice()).isEqualByComparingTo("310.00");
    }

    @Test
    @DisplayName("Scenario 2: Reactively recalculates task and order totals on part updates, removals and approved proposals")
    void reactivelyRecalculatesTotalsOnOperationalMutations() {
        WorkOrder order = newDraftOrder();

        WorkOrderTask task = order.addTask(
                ServiceId.generate(),
                UUID.randomUUID(),
                "Suspension overhaul",
                Money.soles(new BigDecimal("200.00")),
                LaborHours.of(new BigDecimal("2.00"))
        );
        UUID shockAbsorberId = UUID.randomUUID();
        order.addProductToTask(task.getId(), shockAbsorberId, Quantity.of(new BigDecimal("1")), Money.soles(new BigDecimal("150.00")));

        // Initial: 200 + 150 = 350.00, IGV = 63.00, Total = 413.00
        assertThat(task.getConsolidatedPrice().amount()).isEqualByComparingTo("350.00");
        assertThat(order.getSubtotal().amount()).isEqualByComparingTo("350.00");
        assertThat(order.getTax().amount()).isEqualByComparingTo("63.00");
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("413.00");

        // Update part quantity from 1 -> 2: 200 + 300 = 500.00, IGV = 90.00, Total = 590.00
        order.updateProductQuantityInTask(task.getId(), shockAbsorberId, Quantity.of(new BigDecimal("2")), UUID.randomUUID());
        assertThat(task.getConsolidatedPrice().amount()).isEqualByComparingTo("500.00");
        assertThat(order.getSubtotal().amount()).isEqualByComparingTo("500.00");
        assertThat(order.getTax().amount()).isEqualByComparingTo("90.00");
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("590.00");

        // Update authorized task base price from 200 -> 300: 300 + 300 = 600.00, IGV = 108.00, Total = 708.00
        order.updateTaskDetails(task.getId(), "Suspension overhaul + alignment", Money.soles(new BigDecimal("300.00")), null);
        assertThat(task.getConsolidatedPrice().amount()).isEqualByComparingTo("600.00");
        assertThat(order.getSubtotal().amount()).isEqualByComparingTo("600.00");
        assertThat(order.getTax().amount()).isEqualByComparingTo("108.00");
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("708.00");

        // Remove consumed product line: 600 - 300 = 300.00, IGV = 54.00, Total = 354.00
        var productLineId = task.getConsumedProducts().getFirst().getId();
        order.removeProductFromTask(task.getId(), productLineId);
        assertThat(task.getConsolidatedPrice().amount()).isEqualByComparingTo("300.00");
        assertThat(order.getSubtotal().amount()).isEqualByComparingTo("300.00");
        assertThat(order.getTax().amount()).isEqualByComparingTo("54.00");
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("354.00");
    }
}
