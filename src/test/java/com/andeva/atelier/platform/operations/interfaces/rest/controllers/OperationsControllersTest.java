package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.operations.application.commandservices.ServiceCommandService;
import com.andeva.atelier.platform.operations.application.commandservices.WorkBayCommandService;
import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.ServiceQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServicesByTenantIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderTaskByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.*;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Comprehensive Unit Tests for Workshop Operations REST Controllers
 * covering the 36 canonical endpoints defined in docs/backend-documentation/api-endpoints/03-workshop-operations.md.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Workshop Operations REST Controllers Canonical Tests")
class OperationsControllersTest {

    @Mock
    private WorkOrderCommandService workOrderCommandService;
    @Mock
    private WorkOrderQueryService workOrderQueryService;
    @Mock
    private WorkBayCommandService workBayCommandService;
    @Mock
    private WorkBayQueryService workBayQueryService;
    @Mock
    private ServiceCommandService serviceCommandService;
    @Mock
    private ServiceQueryService serviceQueryService;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final BranchId branchId = BranchId.of(UUID.randomUUID());
    private final VehicleId vehicleId = VehicleId.of(UUID.randomUUID());
    private final CustomerId customerId = CustomerId.of(UUID.randomUUID());

    private final CustomUserDetails testUser = new CustomUserDetails(
            UUID.randomUUID(),
            "admin@atelier.pe",
            "password",
            tenantId.value(),
            List.of(),
            true
    );

    private WorkOrder sampleOrder;
    private WorkOrderTask sampleTask;

    @BeforeEach
    void initSampleEntities() {
        sampleOrder = WorkOrder.create(
                tenantId,
                branchId,
                null,
                vehicleId,
                customerId,
                WorkOrderNumber.of(200),
                Mileage.of(25000),
                DiagnosticSummary.of("Diagnóstico preliminar")
        );
        sampleTask = sampleOrder.addTask(
                ServiceId.generate(),
                UUID.randomUUID(),
                "Afinamiento general",
                Money.soles(new BigDecimal("150.00")),
                LaborHours.of(2.0)
        );
    }

    @Nested
    @DisplayName("TasksController Canonical Endpoints Tests")
    class TasksControllerTests {

        private TasksController tasksController;

        @BeforeEach
        void setUp() {
            tasksController = new TasksController(workOrderCommandService, workOrderQueryService);
        }

        @Test
        @DisplayName("Endpoint 3.1: GET /api/v1/tasks/{taskId} should return task details")
        void shouldGetTaskById() {
            when(workOrderQueryService.handle(any(GetWorkOrderTaskByIdQuery.class)))
                    .thenReturn(Optional.of(sampleTask));

            ResponseEntity<?> response = tasksController.getTaskById(sampleTask.getId().value());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Endpoint 3.1: GET /api/v1/tasks/{taskId} not found should return 404")
        void shouldReturnNotFoundForMissingTask() {
            when(workOrderQueryService.handle(any(GetWorkOrderTaskByIdQuery.class)))
                    .thenReturn(Optional.empty());

            ResponseEntity<?> response = tasksController.getTaskById(UUID.randomUUID());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Endpoint 3.2 & 3.3: Task lifecycle start, hold, resume, complete")
        void shouldHandleTaskExecutionLifecycle() {
            when(workOrderCommandService.handle(any(StartWorkOrderTaskCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            ResponseEntity<?> startRes = tasksController.startTask(sampleTask.getId().value());
            assertThat(startRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(HoldWorkOrderTaskCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            HoldTaskResource holdReq = new HoldTaskResource("Falta repuesto", UUID.randomUUID());
            ResponseEntity<?> holdRes = tasksController.holdTask(sampleTask.getId().value(), holdReq);
            assertThat(holdRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(ResumeWorkOrderTaskCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            ResponseEntity<?> resumeRes = tasksController.resumeTask(sampleTask.getId().value());
            assertThat(resumeRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(CompleteWorkOrderTaskCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            CompleteTaskResource compReq = new CompleteTaskResource(new BigDecimal("1.8"), "Culminado");
            ResponseEntity<?> compRes = tasksController.completeTask(sampleTask.getId().value(), compReq);
            assertThat(compRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }


        @Test
        @DisplayName("Endpoint 3.4 & 3.5: Update task, assign mechanic, reopen task")
        void shouldUpdateTaskAndAssignMechanicAndReopen() {
            UUID mechanicId = UUID.randomUUID();
            when(workOrderCommandService.handle(any(AssignTaskMechanicCommand.class)))
                    .thenReturn(Result.success(sampleOrder));

            // updateTask with mechanicId
            when(workOrderQueryService.handle(any(GetWorkOrderTaskByIdQuery.class)))
                    .thenReturn(Optional.of(sampleTask));
            UpdateWorkOrderTaskResource updateReq = new UpdateWorkOrderTaskResource("Nueva descripcion", new BigDecimal("180.00"), mechanicId);
            ResponseEntity<?> updateRes = tasksController.updateTask(sampleTask.getId().value(), updateReq);
            assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            // assignMechanic
            AssignTaskMechanicResource assignReq = new AssignTaskMechanicResource(mechanicId);
            ResponseEntity<?> assignRes = tasksController.assignMechanic(sampleTask.getId().value(), assignReq);
            assertThat(assignRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            // reopenTask
            when(workOrderCommandService.handle(any(ReopenWorkOrderTaskCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            ResponseEntity<?> reopenRes = tasksController.reopenTask(sampleTask.getId().value(), "Rework necesario");
            assertThat(reopenRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Endpoint 3.8 & 3.9: Add and update product on task")
        void shouldAddAndUpdateProductOnTask() {
            when(workOrderCommandService.handle(any(AddProductToTaskCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            AddTaskProductResource addReq = new AddTaskProductResource(UUID.randomUUID(), new BigDecimal("2.0"), new BigDecimal("45.00"), "PEN");
            ResponseEntity<?> addRes = tasksController.addProduct(sampleTask.getId().value(), addReq, UriComponentsBuilder.newInstance());
            assertThat(addRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            when(workOrderCommandService.handle(any(UpdateTaskProductQuantityCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            UpdateTaskProductResource updateReq = new UpdateTaskProductResource(new BigDecimal("3.0"));
            ResponseEntity<?> updateRes = tasksController.updateProductQuantity(sampleTask.getId().value(), addReq.productId(), updateReq);
            assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Endpoint 3.10: DELETE /api/v1/tasks/{taskId}/products/{productId} should return 204 No Content")
        void shouldRemoveTaskProductSuccessfully() {
            when(workOrderCommandService.handle(any(RemoveProductFromTaskCommand.class)))
                    .thenReturn(Result.success(sampleOrder));

            ResponseEntity<?> response = tasksController.removeTaskProduct(sampleTask.getId().value(), UUID.randomUUID());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("Endpoint 3.11: POST /api/v1/tasks/{taskId}/evidence-images should return 201 Created")
        void shouldAttachEvidenceImageSuccessfully() {
            when(workOrderCommandService.handle(any(AttachTaskEvidenceImageCommand.class)))
                    .thenReturn(Result.success(sampleTask));

            AttachTaskEvidenceResource req = new AttachTaskEvidenceResource("https://storage.atelier.pe/evidence/123.jpg", "Foto de foso");
            ResponseEntity<?> response = tasksController.attachEvidence(sampleTask.getId().value(), req, UriComponentsBuilder.newInstance());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("WorkBaysController Canonical Endpoints Tests")
    class WorkBaysControllerTests {

        private WorkBaysController workBaysController;

        @BeforeEach
        void setUp() {
            workBaysController = new WorkBaysController(workBayCommandService, workBayQueryService);
        }

        @Test
        @DisplayName("Endpoint 4.1: POST /api/v1/work-bays should create bay (201)")
        void shouldCreateWorkBay() {
            WorkBay bay = WorkBay.create(tenantId, branchId, "Bahia 1", BayType.LIFT);
            when(workBayCommandService.handle(any(CreateWorkBayCommand.class)))
                    .thenReturn(Result.success(bay));

            CreateWorkBayResource req = new CreateWorkBayResource(branchId.value(), "Bahia 1", "LIFT");
            ResponseEntity<?> response = workBaysController.createBay(testUser, req, UriComponentsBuilder.newInstance());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("Endpoint 4.2 & 4.3: GET work-bays list and getById")
        void shouldGetWorkBays() {
            WorkBay bay = WorkBay.create(tenantId, branchId, "Bahia 1", BayType.LIFT);
            when(workBayQueryService.handle(any(com.andeva.atelier.platform.operations.domain.model.queries.GetWorkBaysByBranchIdQuery.class)))
                    .thenReturn(List.of(bay));

            ResponseEntity<?> listRes = workBaysController.getBays(testUser, branchId.value(), false);
            assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workBayQueryService.getById(any(WorkBayId.class))).thenReturn(Optional.of(bay));
            ResponseEntity<?> byIdRes = workBaysController.getBayById(bay.getId().value());
            assertThat(byIdRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Endpoint 4.4 & 4.5: PUT /api/v1/work-bays/{bayId}/maintenance and restore")
        void shouldSetMaintenanceAndRestoreBay() {
            WorkBay bay = WorkBay.create(tenantId, branchId, "Bahia 1", BayType.LIFT);
            when(workBayCommandService.handle(any(UpdateWorkBayStatusCommand.class)))
                    .thenReturn(Result.success(bay));

            MaintenanceBayResource maintReq = new MaintenanceBayResource("Mantenimiento preventivo de elevador");
            ResponseEntity<?> maintRes = workBaysController.setMaintenance(bay.getId().value(), maintReq);
            assertThat(maintRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            ResponseEntity<?> restoreRes = workBaysController.restoreAvailable(bay.getId().value());
            assertThat(restoreRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("WorkOrdersController Canonical Endpoints Tests")
    class WorkOrdersControllerTests {

        private WorkOrdersController workOrdersController;

        @BeforeEach
        void setUp() {
            workOrdersController = new WorkOrdersController(
                    workOrderCommandService,
                    workOrderQueryService,
                    workBayQueryService
            );
        }

        @Test
        @DisplayName("Endpoint 2.1: POST /api/v1/work-orders should create work order (201)")
        void shouldCreateWorkOrder() {
            when(workOrderCommandService.handle(any(CreateWorkOrderCommand.class)))
                    .thenReturn(Result.success(sampleOrder));

            CreateWorkOrderResource req = new CreateWorkOrderResource(null, vehicleId.value(), 25000, "Revisión");
            ResponseEntity<?> response = workOrdersController.createWorkOrder(testUser, req, UriComponentsBuilder.newInstance());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("Endpoint 2.2 & 2.3: GET work orders list and getById")
        void shouldGetWorkOrders() {
            when(workOrderQueryService.handle(any(com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrdersByTenantIdQuery.class)))
                    .thenReturn(List.of(sampleOrder));

            ResponseEntity<?> listRes = workOrdersController.getWorkOrders(testUser, null);
            assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class)))
                    .thenReturn(Optional.of(sampleOrder));

            ResponseEntity<?> byIdRes = workOrdersController.getWorkOrderById(sampleOrder.getId().value());
            assertThat(byIdRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }


        @Test
        @DisplayName("Endpoint 2.4: PUT /api/v1/work-orders/{workOrderId} update diagnostic and mileage")
        void shouldUpdateWorkOrder() {
            when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class)))
                    .thenReturn(Optional.of(sampleOrder));

            UpdateWorkOrderResource updateReq = new UpdateWorkOrderResource(28000, "Diagnóstico actualizado");
            ResponseEntity<?> updateRes = workOrdersController.updateWorkOrder(sampleOrder.getId().value(), updateReq);
            assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Endpoint 2.7: POST /api/v1/work-orders/{workOrderId}/tasks add task")
        void shouldCreateTaskOnWorkOrder() {
            when(workOrderCommandService.handle(any(AddTaskToWorkOrderCommand.class)))
                    .thenReturn(Result.success(sampleOrder));

            CreateWorkOrderTaskResource createReq = new CreateWorkOrderTaskResource(
                    ServiceId.generate().value(),
                    UUID.randomUUID(),
                    "Cambio de pastillas",
                    new BigDecimal("80.00"),
                    "PEN"
            );
            ResponseEntity<?> createRes = workOrdersController.createTask(sampleOrder.getId().value(), createReq, UriComponentsBuilder.newInstance());
            assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("Endpoint 2.9: GET /api/v1/work-orders/{workOrderId}/proposals list proposals")
        void shouldGetWorkOrderProposals() {
            TaskProposal proposal = TaskProposal.create(
                    sampleOrder.getId(),
                    UUID.randomUUID(),
                    "Fuga",
                    ProposalSeverity.MEDIUM,
                    StorageUrl.of("https://storage.atelier.pe/prop/2.jpg"),
                    ServiceId.generate()
            );
            when(workOrderQueryService.handle(any(com.andeva.atelier.platform.operations.domain.model.queries.GetTaskProposalsByWorkOrderIdQuery.class)))
                    .thenReturn(List.of(proposal));

            ResponseEntity<?> listRes = workOrdersController.getProposals(sampleOrder.getId().value());
            assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(listRes.getBody()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("Endpoint 2.12: POST /api/v1/work-orders/{workOrderId}/intake-images attach intake image")
        void shouldAttachIntakeImage() {
            when(workOrderCommandService.handle(any(AttachIntakeImageCommand.class)))
                    .thenReturn(Result.success(sampleOrder));

            AttachImageResource attachReq = new AttachImageResource("https://storage.atelier.pe/intake/front.jpg", "Golpe en parachoque");
            ResponseEntity<?> attachRes = workOrdersController.attachIntakeImage(sampleOrder.getId().value(), attachReq, UriComponentsBuilder.newInstance());
            assertThat(attachRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("Endpoint 2.5 & 2.6: Assign and release work bay")
        void shouldAssignAndReleaseBay() {
            when(workOrderCommandService.handle(any(AssignWorkBayCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            AssignWorkBayResource req = new AssignWorkBayResource(UUID.randomUUID());
            ResponseEntity<?> assignRes = workOrdersController.assignBay(sampleOrder.getId().value(), req);
            assertThat(assignRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(ReleaseWorkBayCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            ResponseEntity<?> releaseRes = workOrdersController.releaseBay(sampleOrder.getId().value());
            assertThat(releaseRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("Endpoint 2.8, 2.10, 2.11: Defect proposals lifecycle")
        void shouldManageTaskProposals() {
            TaskProposal proposal = TaskProposal.create(
                    sampleOrder.getId(),
                    UUID.randomUUID(),
                    "Fuga de refrigerante en manguera superior",
                    ProposalSeverity.HIGH,
                    StorageUrl.of("https://storage.atelier.pe/prop/1.jpg"),
                    ServiceId.generate()
            );
            when(workOrderCommandService.handle(any(SubmitTaskProposalCommand.class)))
                    .thenReturn(Result.success(proposal));

            SubmitTaskProposalResource submitReq = new SubmitTaskProposalResource(UUID.randomUUID(), "Fuga", "HIGH", "https://img.com", UUID.randomUUID());
            ResponseEntity<?> submitRes = workOrdersController.submitProposal(sampleOrder.getId().value(), submitReq, UriComponentsBuilder.newInstance());
            assertThat(submitRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            when(workOrderCommandService.handle(any(ApproveTaskProposalCommand.class)))
                    .thenReturn(Result.success(sampleTask));
            ApproveTaskProposalResource appReq = new ApproveTaskProposalResource(UUID.randomUUID(), new BigDecimal("100"), new BigDecimal("1.0"), UUID.randomUUID(), "Aprobado");
            ResponseEntity<?> appRes = workOrdersController.approveProposal(sampleOrder.getId().value(), proposal.getId(), appReq);
            assertThat(appRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(RejectTaskProposalCommand.class)))
                    .thenReturn(Result.success(proposal));
            RejectTaskProposalResource rejReq = new RejectTaskProposalResource("Cliente no autoriza");
            ResponseEntity<?> rejRes = workOrdersController.rejectProposal(sampleOrder.getId().value(), proposal.getId(), rejReq);
            assertThat(rejRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Endpoint 2.13, 2.14, 2.15, 2.16: WorkOrder lifecycle transitions")
        void shouldHandleWorkOrderTransitions() {
            when(workOrderCommandService.handle(any(StartWorkOrderCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            assertThat(workOrdersController.startWorkOrder(sampleOrder.getId().value()).getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(CompleteWorkOrderCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            assertThat(workOrdersController.completeWorkOrder(sampleOrder.getId().value()).getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(MarkWorkOrderAsPaidCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            assertThat(workOrdersController.markAsPaid(sampleOrder.getId().value()).getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(CancelWorkOrderCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            CancelWorkOrderResource cancelReq = new CancelWorkOrderResource("Cancelado por cliente");
            assertThat(workOrdersController.cancelWorkOrder(sampleOrder.getId().value(), cancelReq).getStatusCode()).isEqualTo(HttpStatus.OK);

            when(workOrderCommandService.handle(any(DeliverVehicleCommand.class)))
                    .thenReturn(Result.success(sampleOrder));
            assertThat(workOrdersController.deliverVehicle(sampleOrder.getId().value()).getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("ServicesController Canonical Endpoints Tests")
    class ServicesControllerTests {

        private ServicesController servicesController;

        @BeforeEach
        void setUp() {
            servicesController = new ServicesController(serviceCommandService, serviceQueryService);
        }


        @Test
        @DisplayName("Endpoint 5.2 & 5.3: GET services list and getServiceById")
        void shouldGetServicesAndById() {
            Service service = Service.create(tenantId, "Afinamiento", Money.soles(new BigDecimal("120.00")), 60);
            when(serviceQueryService.handle(any(GetServicesByTenantIdQuery.class)))
                    .thenReturn(List.of(service));

            ResponseEntity<?> listRes = servicesController.getServices(testUser);
            assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);

            when(serviceQueryService.handle(any(com.andeva.atelier.platform.operations.domain.model.queries.GetServiceByIdQuery.class)))
                    .thenReturn(Optional.of(service));

            ResponseEntity<?> byIdRes = servicesController.getServiceById(service.getId().value());
            assertThat(byIdRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Endpoint 5.1 & 5.4: Create and update catalog services")
        void shouldCreateAndUpdateService() {
            Service service = Service.create(tenantId, "Afinamiento", Money.soles(new BigDecimal("120.00")), 60);
            when(serviceCommandService.handle(any(CreateServiceItemCommand.class)))
                    .thenReturn(Result.success(service));

            CreateServiceResource createReq = new CreateServiceResource("Afinamiento", new BigDecimal("120.00"), "PEN", 60);
            ResponseEntity<?> createRes = servicesController.createService(testUser, createReq, UriComponentsBuilder.newInstance());
            assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            when(serviceCommandService.handle(any(UpdateServiceItemCommand.class)))
                    .thenReturn(Result.success(service));
            UpdateServiceResource updateReq = new UpdateServiceResource("Afinamiento Premium", new BigDecimal("150.00"), "PEN", 75);
            ResponseEntity<?> updateRes = servicesController.updateService(service.getId().value(), updateReq);
            assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
