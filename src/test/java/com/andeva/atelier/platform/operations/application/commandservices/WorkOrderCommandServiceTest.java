package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.application.internal.commandservices.WorkOrderCommandServiceImpl;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
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
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
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
}
