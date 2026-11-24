package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.application.internal.commandservices.WorkOrderCommandServiceImpl;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkOrder Command Service Orchestration Tests")
class WorkOrderCommandServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkBayRepository workBayRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private DirectToCloudStorageGateway storageGateway;

    private WorkOrderCommandServiceImpl commandService;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());
    private final VehicleId vehicleId = VehicleId.of(UUID.randomUUID());
    private final CustomerId customerId = CustomerId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        commandService = new WorkOrderCommandServiceImpl(
                workOrderRepository,
                workBayRepository,
                serviceRepository,
                storageGateway
        );
    }

    @Test
    @DisplayName("Should successfully create a WorkOrder in DRAFT status")
    void shouldCreateWorkOrderSuccessfully() {
        when(workOrderRepository.findNextInternalSequence(tenantId)).thenReturn(105);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWorkOrderCommand command = new CreateWorkOrderCommand(
                tenantId,
                branchId,
                null,
                vehicleId,
                customerId,
                Mileage.of(20000),
                DiagnosticSummary.of("Revisión de frenos y afinamiento")
        );

        Result<WorkOrder, ApplicationError> result = commandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        WorkOrder created = result.getOrThrow();
        assertThat(created.getInternalNumber().sequence()).isEqualTo(105);
        assertThat(created.getStatus()).isEqualTo(WorkOrderStatus.DRAFT);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    @DisplayName("Should assign WorkBay to WorkOrder when bay is available")
    void shouldAssignWorkBaySuccessfully() {
        WorkOrder order = WorkOrder.create(
                tenantId,
                branchId,
                null,
                vehicleId,
                customerId,
                WorkOrderNumber.of(1),
                Mileage.of(10000),
                DiagnosticSummary.of("Diagnóstico preliminar")
        );
        WorkBay bay = WorkBay.create(tenantId, branchId, "Bahía 1", BayType.LIFT);

        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(workBayRepository.findById(bay.getId())).thenReturn(Optional.of(bay));
        when(workBayRepository.save(any(WorkBay.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(new AssignWorkBayCommand(order.getId(), bay.getId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getCurrentBayId()).contains(bay.getId());
    }

    @Test
    @DisplayName("Should return notFound when assigning non-existent bay")
    void shouldReturnNotFoundWhenBayDoesNotExist() {
        WorkOrder order = WorkOrder.create(
                tenantId,
                branchId,
                null,
                vehicleId,
                customerId,
                WorkOrderNumber.of(1),
                Mileage.of(10000),
                DiagnosticSummary.of("Diagnóstico preliminar")
        );
        WorkBayId nonExistentBayId = WorkBayId.generate();

        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(workBayRepository.findById(nonExistentBayId)).thenReturn(Optional.empty());

        Result<WorkOrder, ApplicationError> result = commandService.handle(new AssignWorkBayCommand(order.getId(), nonExistentBayId));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).contains("NOT_FOUND");
    }

    @Test
    @DisplayName("Should assign mechanic to task via findByTaskId")
    void shouldAssignTaskMechanicSuccessfully() {
        WorkOrder order = WorkOrder.create(
                tenantId,
                branchId,
                null,
                vehicleId,
                customerId,
                WorkOrderNumber.of(1),
                Mileage.of(10000),
                DiagnosticSummary.of("Diagnóstico preliminar")
        );
        WorkOrderTask task = order.addTask(
                ServiceId.generate(),
                null,
                "Cambio de pastillas de freno",
                Money.soles(120),
                LaborHours.of(1.5)
        );
        UUID mechanicId = UUID.randomUUID();

        when(workOrderRepository.findByTaskId(task.getId())).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(new AssignTaskMechanicCommand(task.getId(), mechanicId));

        assertThat(result.isSuccess()).isTrue();
        WorkOrder updated = result.getOrThrow();
        WorkOrderTask updatedTask = updated.getTasks().stream()
                .filter(t -> t.getId().equals(task.getId()))
                .findFirst().orElseThrow();
        assertThat(updatedTask.getMechanicId()).contains(mechanicId);
        assertThat(updatedTask.getStatus()).isEqualTo(WorkOrderTaskStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Should handle full task lifecycle: start, hold, resume, and complete")
    void shouldHandleFullTaskLifecycleSuccessfully() {
        WorkOrder order = WorkOrder.create(
                tenantId,
                branchId,
                null,
                vehicleId,
                customerId,
                WorkOrderNumber.of(1),
                Mileage.of(10000),
                DiagnosticSummary.of("Diagnóstico preliminar")
        );
        WorkOrderTask task = order.addTask(
                ServiceId.generate(),
                UUID.randomUUID(),
                "Alineación y balanceo",
                Money.soles(80),
                LaborHours.of(1.0)
        );

        when(workOrderRepository.findByTaskId(task.getId())).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Start task
        Result<WorkOrder, ApplicationError> startResult = commandService.handle(new StartWorkOrderTaskCommand(task.getId()));
        assertThat(startResult.isSuccess()).isTrue();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.IN_PROGRESS);

        // 2. Request suspension (task keeps running) and approve it (task goes on hold)
        Result<WorkOrder, ApplicationError> holdResult = commandService.handle(new HoldWorkOrderTaskCommand(task.getId(), "Falta terminal de dirección", UUID.randomUUID()));
        assertThat(holdResult.isSuccess()).isTrue();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.IN_PROGRESS);
        assertThat(task.hasPendingSuspensionRequest()).isTrue();

        Result<WorkOrder, ApplicationError> approveResult = commandService.handle(new ApproveTaskSuspensionCommand(task.getId()));
        assertThat(approveResult.isSuccess()).isTrue();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.ON_HOLD);

        // 3. Resume task
        Result<WorkOrder, ApplicationError> resumeResult = commandService.handle(new ResumeWorkOrderTaskCommand(task.getId()));
        assertThat(resumeResult.isSuccess()).isTrue();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.IN_PROGRESS);

        // 4. Complete task
        Result<WorkOrder, ApplicationError> completeResult = commandService.handle(new CompleteWorkOrderTaskCommand(task.getId(), new java.math.BigDecimal("1.25"), "Labor culminada exitosamente"));
        assertThat(completeResult.isSuccess()).isTrue();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should persist updated mileage and diagnostic summary on UpdateWorkOrderCommand")
    void shouldPersistUpdatedMileageAndDiagnosticSummary() {
        WorkOrder order = WorkOrder.create(
                tenantId, branchId, null, vehicleId, customerId,
                WorkOrderNumber.of(12), Mileage.of(0), DiagnosticSummary.of("Ingreso por cita programada")
        );

        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(new UpdateWorkOrderCommand(
                order.getId(),
                DiagnosticSummary.of("Ruido en suspensión delantera"),
                Mileage.of(48250)
        ));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrThrow().getMileageIn().value()).isEqualTo(48250);
        assertThat(result.getOrThrow().getDiagnosticSummary().value()).isEqualTo("Ruido en suspensión delantera");
        verify(workOrderRepository).save(order);
    }

    @Test
    @DisplayName("Should update task description, price and recalculate totals on UpdateWorkOrderTaskCommand")
    void shouldUpdateTaskDescriptionPriceAndRecalculateTotals() {
        WorkOrder order = WorkOrder.create(
                tenantId, branchId, null, vehicleId, customerId,
                WorkOrderNumber.of(13), Mileage.of(32000), DiagnosticSummary.of("Mantenimiento preventivo")
        );
        WorkOrderTask task = order.addTask(
                ServiceId.generate(), null, "Cambio de aceite", Money.soles(100), LaborHours.of(1.0)
        );
        UUID mechanicId = UUID.randomUUID();

        when(workOrderRepository.findByTaskId(task.getId())).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(new UpdateWorkOrderTaskCommand(
                task.getId(),
                "Cambio de aceite sintético 5W-30 y filtro",
                new java.math.BigDecimal("165.00"),
                mechanicId
        ));

        assertThat(result.isSuccess()).isTrue();
        assertThat(task.getDescription()).isEqualTo("Cambio de aceite sintético 5W-30 y filtro");
        assertThat(task.getPrice().amount()).isEqualByComparingTo("165.00");
        assertThat(task.getMechanicId()).contains(mechanicId);
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("194.70");
        verify(workOrderRepository).save(order);
    }

    @Test
    @DisplayName("Should self-heal orphaned OCCUPIED bay with no open work order on AssignWorkBayCommand")
    void shouldSelfHealOrphanedOccupiedBayOnAssign() {
        WorkOrder order = WorkOrder.create(
                tenantId, branchId, null, vehicleId, customerId,
                WorkOrderNumber.of(14), Mileage.of(15000), DiagnosticSummary.of("Revisión de frenos")
        );
        WorkBay orphanedBay = new WorkBay(
                WorkBayId.generate(), tenantId, branchId, "Elevador 1", BayType.LIFT, BayStatus.OCCUPIED, null
        );

        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(workBayRepository.findById(orphanedBay.getId())).thenReturn(Optional.of(orphanedBay));
        when(workOrderRepository.findByCurrentBayId(orphanedBay.getId())).thenReturn(Optional.empty());
        when(workBayRepository.save(any(WorkBay.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(
                new AssignWorkBayCommand(order.getId(), orphanedBay.getId())
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getCurrentBayId()).contains(orphanedBay.getId());
        assertThat(orphanedBay.getStatus()).isEqualTo(BayStatus.OCCUPIED);
        assertThat(orphanedBay.getCurrentWorkOrderId()).contains(order.getId());
    }

    @Test
    @DisplayName("Should return conflict when bay is occupied by an active open work order")
    void shouldRejectAssigningBayOccupiedByActiveWorkOrder() {
        WorkOrder newOrder = WorkOrder.create(
                tenantId, branchId, null, vehicleId, customerId,
                WorkOrderNumber.of(15), Mileage.of(15000), DiagnosticSummary.of("Alineación")
        );
        WorkBay occupiedBay = new WorkBay(
                WorkBayId.generate(), tenantId, branchId, "Elevador 2", BayType.LIFT, BayStatus.OCCUPIED, null
        );
        WorkOrder activeOrder = WorkOrder.create(
                tenantId, branchId, null, vehicleId, customerId,
                WorkOrderNumber.of(16), Mileage.of(22000), DiagnosticSummary.of("Motor en reparación")
        );
        activeOrder.assignWorkBay(occupiedBay.getId());

        when(workOrderRepository.findById(newOrder.getId())).thenReturn(Optional.of(newOrder));
        when(workBayRepository.findById(occupiedBay.getId())).thenReturn(Optional.of(occupiedBay));
        when(workOrderRepository.findByCurrentBayId(occupiedBay.getId())).thenReturn(Optional.of(activeOrder));

        Result<WorkOrder, ApplicationError> result = commandService.handle(
                new AssignWorkBayCommand(newOrder.getId(), occupiedBay.getId())
        );

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).contains("CONFLICT");
    }

    @Test
    @DisplayName("Should release work bay when completing the last task auto-completes the order")
    void shouldReleaseWorkBayWhenLastTaskAutoCompletesOrder() {
        WorkOrder order = WorkOrder.create(
                tenantId, branchId, null, vehicleId, customerId,
                WorkOrderNumber.of(17), Mileage.of(18000), DiagnosticSummary.of("Cambio de bujías")
        );
        WorkBay bay = WorkBay.create(tenantId, branchId, "Elevador 1", BayType.LIFT);
        bay.occupy(order.getId());
        order.assignWorkBay(bay.getId());
        WorkOrderTask task = order.addTask(
                ServiceId.generate(), UUID.randomUUID(), "Cambio de bujías", Money.soles(90), LaborHours.of(1.0)
        );
        order.startTask(task.getId());

        when(workOrderRepository.findByTaskId(task.getId())).thenReturn(Optional.of(order));
        when(workBayRepository.findById(bay.getId())).thenReturn(Optional.of(bay));
        when(workBayRepository.save(any(WorkBay.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(
                new CompleteWorkOrderTaskCommand(task.getId(), new java.math.BigDecimal("1.0"), "Listo")
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(order.getCurrentBayId()).isEmpty();
        assertThat(bay.getStatus()).isEqualTo(BayStatus.AVAILABLE);
        verify(workBayRepository).save(bay);
    }

    @Test
    @DisplayName("Should release work bay when delivering vehicle")
    void shouldReleaseWorkBayWhenVehicleIsDelivered() {
        WorkOrder order = WorkOrder.create(
                tenantId, branchId, null, vehicleId, customerId,
                WorkOrderNumber.of(18), Mileage.of(25000), DiagnosticSummary.of("Afinamiento")
        );
        WorkBay bay = WorkBay.create(tenantId, branchId, "Elevador 3", BayType.LIFT);
        bay.occupy(order.getId());
        order.assignWorkBay(bay.getId());
        WorkOrderTask task = order.addTask(
                ServiceId.generate(), UUID.randomUUID(), "Afinamiento", Money.soles(150), LaborHours.of(1.0)
        );
        order.startTask(task.getId());
        order.completeTask(task.getId(), LaborHours.of(1.0));
        order.markPaid();

        when(workOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(workBayRepository.findById(bay.getId())).thenReturn(Optional.of(bay));
        when(workBayRepository.save(any(WorkBay.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<WorkOrder, ApplicationError> result = commandService.handle(new DeliverVehicleCommand(order.getId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getCurrentBayId()).isEmpty();
        assertThat(bay.getStatus()).isEqualTo(BayStatus.AVAILABLE);
        verify(workBayRepository).save(bay);
    }
}
