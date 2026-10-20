package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.application.internal.commandservices.WorkOrderCommandServiceImpl;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryItemSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.AddProductToTaskCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.AddTaskToWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkOrder custody, stock and catalog pricing rules")
class WorkOrderCustodyAndPricingRulesTest {

    @Mock
    private WorkOrderRepository workOrderRepository;
    @Mock
    private WorkBayRepository workBayRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private DirectToCloudStorageGateway storageGateway;
    @Mock
    private InventoryReservationAclService inventoryAclService;

    private WorkOrderCommandServiceImpl commandService;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());
    private final VehicleId vehicleId = VehicleId.of(UUID.randomUUID());
    private final CustomerId customerId = CustomerId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        commandService = new WorkOrderCommandServiceImpl(
                workOrderRepository, workBayRepository, serviceRepository, storageGateway, null, null, inventoryAclService);
    }

    private WorkOrder draftOrder() {
        return WorkOrder.create(tenantId, branchId, null, vehicleId, customerId, WorkOrderNumber.of(7),
                Mileage.of(10000), DiagnosticSummary.of("Revisión"));
    }

    @Test
    @DisplayName("Rejects a second open work order for the same vehicle with 409")
    void shouldRejectSecondOpenOrderForVehicle() {
        when(workOrderRepository.findOpenByVehicleId(tenantId, vehicleId)).thenReturn(Optional.of(draftOrder()));

        Result<WorkOrder, ApplicationError> result = commandService.handle(new CreateWorkOrderCommand(
                tenantId, branchId, null, vehicleId, customerId, Mileage.of(10000), DiagnosticSummary.of("Duplicada")));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Copies the catalog labor price and duration when the task omits them")
    void shouldDefaultTaskPriceAndHoursFromCatalog() {
        WorkOrder order = draftOrder();
        Service service = Service.create(tenantId, "Cambio de aceite", Money.soles(new BigDecimal("80.00")), 90);
        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(new AddTaskToWorkOrderCommand(
                order.getId(), service.getId(), null, "Cambio de aceite", null, null, "PEN"));

        assertThat(result.isSuccess()).isTrue();
        WorkOrderTask task = result.getOrThrow().getTasks().getFirst();
        assertThat(task.getPrice().amount()).isEqualByComparingTo("80.00");
        assertThat(task.getEstimatedHours().value()).isEqualByComparingTo("1.50");
    }

    @Test
    @DisplayName("Rejects a part line without enough stock with 409 before touching the order")
    void shouldRejectPartWithoutStock() {
        WorkOrder order = draftOrder();
        WorkOrderTask task = order.addTask(com.andeva.atelier.platform.operations.domain.model.ids.ServiceId.of(UUID.randomUUID()),
                null, "Filtro", Money.soles(new BigDecimal("50.00")), LaborHours.of(BigDecimal.ONE));
        UUID partId = UUID.randomUUID();
        when(workOrderRepository.findByTaskId(task.getId())).thenReturn(Optional.of(order));
        when(inventoryAclService.fetchItemDetails(tenantId.value(), partId)).thenReturn(Optional.of(
                new InventoryItemSummaryDto(partId, "FIL-01", "Filtro", BigDecimal.ONE, BigDecimal.ZERO, "UNIDADES", "PEN", new BigDecimal("35.00"))));
        when(inventoryAclService.checkItemStockAvailability(tenantId.value(), partId, new BigDecimal("2"))).thenReturn(false);

        Result<WorkOrder, ApplicationError> result = commandService.handle(
                new AddProductToTaskCommand(task.getId(), partId, new BigDecimal("2"), null, "PEN"));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        assertThat(result.getError().message()).contains("requested 2, available 1");
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Copies the catalog sale price of the part when the line omits it")
    void shouldDefaultPartPriceFromCatalog() {
        WorkOrder order = draftOrder();
        WorkOrderTask task = order.addTask(com.andeva.atelier.platform.operations.domain.model.ids.ServiceId.of(UUID.randomUUID()),
                null, "Filtro", Money.soles(new BigDecimal("50.00")), LaborHours.of(BigDecimal.ONE));
        UUID partId = UUID.randomUUID();
        when(workOrderRepository.findByTaskId(task.getId())).thenReturn(Optional.of(order));
        when(inventoryAclService.fetchItemDetails(tenantId.value(), partId)).thenReturn(Optional.of(
                new InventoryItemSummaryDto(partId, "FIL-01", "Filtro", BigDecimal.TEN, BigDecimal.ZERO, "UNIDADES", "PEN", new BigDecimal("12.00"))));
        when(inventoryAclService.checkItemStockAvailability(tenantId.value(), partId, new BigDecimal("2"))).thenReturn(true);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(
                new AddProductToTaskCommand(task.getId(), partId, new BigDecimal("2"), null, "PEN"));

        assertThat(result.isSuccess()).isTrue();
        var line = result.getOrThrow().getTasks().getFirst().getConsumedProducts().getFirst();
        assertThat(line.getUnitPrice().amount()).isEqualByComparingTo("12.00");
    }
}
