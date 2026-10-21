package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.application.internal.commandservices.WorkOrderCommandServiceImpl;
import com.andeva.atelier.platform.operations.application.internal.ledgerservices.WorkOrderPartLedgerServiceImpl;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderPartLedgerAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderSettlementAclService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.MarkWorkOrderAsPaidCommand;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskPauseEndedEvent;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Paid only once settled (US39), resume notice (US27) and parts cost of sales (US35)")
class WorkOrderSettlementAndLedgerTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final WorkOrderRepository workOrderRepository = mock(WorkOrderRepository.class);

    private WorkOrder order() {
        return WorkOrder.create(tenantId, BranchId.of(UUID.randomUUID()), null, VehicleId.of(UUID.randomUUID()),
                CustomerId.of(UUID.randomUUID()), WorkOrderNumber.of(7), Mileage.of(1000), DiagnosticSummary.of("Afinamiento"));
    }

    private WorkOrder completedOrder() {
        WorkOrder order = order();
        WorkOrderTask task = order.addTask(ServiceId.of(UUID.randomUUID()), UUID.randomUUID(), "Cambio de bujías",
                Money.soles(new BigDecimal("50.00")), LaborHours.of(BigDecimal.ONE));
        order.startTask(task.getId());
        order.completeTask(task.getId(), LaborHours.of(BigDecimal.ONE));
        return order;
    }

    private WorkOrderCommandServiceImpl serviceWith(boolean settled) {
        WorkOrderSettlementAclService settlement = workOrderId -> settled;
        return new WorkOrderCommandServiceImpl(workOrderRepository, mock(WorkBayRepository.class), mock(ServiceRepository.class),
                mock(DirectToCloudStorageGateway.class), null, null, null, null, settlement);
    }

    @Test
    @DisplayName("A completed order with a pending voucher balance cannot be marked as paid")
    void shouldRejectPaidWhileVoucherIsPending() {
        WorkOrder order = completedOrder();
        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        var result = serviceWith(false).handle(new MarkWorkOrderAsPaidCommand(order.getId()));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("UNPROCESSABLE_ENTITY");
        assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Once the voucher is settled the order is marked as paid")
    void shouldMarkPaidWhenSettled() {
        WorkOrder order = completedOrder();
        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = serviceWith(true).handle(new MarkWorkOrderAsPaidCommand(order.getId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.PAID);
    }

    @Test
    @DisplayName("Restarting a task after a break announces the end of the pause")
    void shouldAnnounceEndOfPause() {
        WorkOrder order = order();
        WorkOrderTask task = order.addTask(ServiceId.of(UUID.randomUUID()), UUID.randomUUID(), "Frenos",
                Money.soles(new BigDecimal("80.00")), LaborHours.of(BigDecimal.ONE));
        order.startTask(task.getId());
        order.pauseTask(task.getId(), HoldReason.BREAK);

        order.startTask(task.getId());

        assertThat(order.domainEvents()).anyMatch(e -> e instanceof WorkOrderTaskPauseEndedEvent ended
                && ended.pauseReason() == HoldReason.BREAK && ended.tenantId().equals(tenantId));
    }

    @Test
    @DisplayName("Three plugs sold at S/12 that cost S/17 by FIFO leave S/19 of gross profit")
    void shouldComputePartsGrossProfit() {
        WorkOrder order = order();
        WorkOrderTask task = order.addTask(ServiceId.of(UUID.randomUUID()), UUID.randomUUID(), "Cambio de bujías",
                Money.soles(new BigDecimal("50.00")), LaborHours.of(BigDecimal.ONE));
        UUID plug = UUID.randomUUID();
        order.addProductToTask(task.getId(), plug, Quantity.of(new BigDecimal("3")), Money.soles(new BigDecimal("12.00")));
        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        WorkOrderPartLedgerAclService ledger = new WorkOrderPartLedgerAclService() {
            @Override
            public List<PartCost> costsOf(UUID workOrderId) {
                return List.of(
                        new PartCost(plug, task.getId().value(), UUID.randomUUID(), new BigDecimal("2"), new BigDecimal("5.00"), new BigDecimal("10.00"), "PEN"),
                        new PartCost(plug, task.getId().value(), UUID.randomUUID(), new BigDecimal("1"), new BigDecimal("7.00"), new BigDecimal("7.00"), "PEN"));
            }

            @Override
            public List<PartMovement> movementsOf(UUID workOrderId) {
                return List.of();
            }
        };

        var profitability = new WorkOrderPartLedgerServiceImpl(workOrderRepository, ledger)
                .profitability(tenantId, order.getId()).getOrThrow();

        assertThat(profitability.partsRevenue()).isEqualByComparingTo("36.00");
        assertThat(profitability.partsCostOfSales()).isEqualByComparingTo("17.00");
        assertThat(profitability.partsGrossProfit()).isEqualByComparingTo("19.00");
        assertThat(profitability.laborRevenue()).isEqualByComparingTo("50.00");
        assertThat(profitability.lines()).singleElement().satisfies(line -> assertThat(line.unitPrice()).isEqualByComparingTo("12.00"));
    }

    @Test
    @DisplayName("Another workshop's order is not found")
    void shouldHideOtherTenantsOrders() {
        WorkOrder order = order();
        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        WorkOrderPartLedgerAclService ledger = mock(WorkOrderPartLedgerAclService.class);

        var result = new WorkOrderPartLedgerServiceImpl(workOrderRepository, ledger)
                .partMovements(TenantId.of(UUID.randomUUID()), order.getId());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("NOT_FOUND");
    }
}
