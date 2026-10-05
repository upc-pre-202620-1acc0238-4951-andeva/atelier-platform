package com.andeva.atelier.platform.operations.application.internal.commandservices;

import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerFleetAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class WorkOrderCommandServiceImpl implements WorkOrderCommandService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkBayRepository workBayRepository;
    private final ServiceRepository serviceRepository;
    private final DirectToCloudStorageGateway storageGateway;
    private final CustomerFleetAclService customerFleetAclService;
    private final TenancyAclService tenancyAclService;
    private final InventoryReservationAclService inventoryAclService;

    public WorkOrderCommandServiceImpl(
            WorkOrderRepository workOrderRepository,
            WorkBayRepository workBayRepository,
            ServiceRepository serviceRepository,
            DirectToCloudStorageGateway storageGateway
    ) {
        this(workOrderRepository, workBayRepository, serviceRepository, storageGateway, null, null, null);
    }

    public WorkOrderCommandServiceImpl(
            WorkOrderRepository workOrderRepository,
            WorkBayRepository workBayRepository,
            ServiceRepository serviceRepository,
            DirectToCloudStorageGateway storageGateway,
            CustomerFleetAclService customerFleetAclService,
            TenancyAclService tenancyAclService,
            InventoryReservationAclService inventoryAclService
    ) {
        this.workOrderRepository = Objects.requireNonNull(workOrderRepository);
        this.workBayRepository = Objects.requireNonNull(workBayRepository);
        this.serviceRepository = Objects.requireNonNull(serviceRepository);
        this.storageGateway = Objects.requireNonNull(storageGateway);
        this.customerFleetAclService = customerFleetAclService;
        this.tenancyAclService = tenancyAclService;
        this.inventoryAclService = inventoryAclService;
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(CreateWorkOrderCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.badRequest("CreateWorkOrderCommand cannot be null"));
        }

        Integer nextSeq = workOrderRepository.findNextInternalSequence(command.tenantId());
        WorkOrderNumber internalNumber = WorkOrderNumber.of(nextSeq != null ? nextSeq : 1);

        UUID appointmentId = command.appointmentId() != null ? command.appointmentId().value() : null;

        WorkOrder workOrder = WorkOrder.create(
                command.tenantId(),
                command.branchId(),
                appointmentId,
                command.vehicleId(),
                command.customerId(),
                internalNumber,
                command.mileageIn(),
                command.diagnosticSummary()
        );

        WorkOrder saved = workOrderRepository.save(workOrder);
        return Result.success(saved);
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(AssignWorkBayCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        Optional<WorkBay> bayOpt = workBayRepository.findById(command.bayId());
        if (bayOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkBay with identifier " + command.bayId().value() + " was not found"));
        }

        WorkOrder workOrder = orderOpt.get();
        WorkBay bay = bayOpt.get();

        if (bay.getStatus() != BayStatus.AVAILABLE) {
            return Result.failure(ApplicationError.conflict("Work bay " + bay.getId().value() + " is not available (status: " + bay.getStatus() + ")"));
        }

        workOrder.getCurrentBayId().ifPresent(prevBayId -> {
            workBayRepository.findById(prevBayId).ifPresent(prevBay -> {
                prevBay.release();
                workBayRepository.save(prevBay);
            });
        });

        bay.occupy(workOrder.getId());
        workOrder.assignWorkBay(bay.getId());

        workBayRepository.save(bay);
        WorkOrder saved = workOrderRepository.save(workOrder);
        return Result.success(saved);
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(ReleaseWorkBayCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder workOrder = orderOpt.get();
        if (workOrder.getCurrentBayId().isEmpty()) {
            return Result.failure(ApplicationError.badRequest("Work order does not have an assigned bay to release"));
        }

        workBayRepository.findById(workOrder.getCurrentBayId().get()).ifPresent(bay -> {
            bay.release();
            workBayRepository.save(bay);
        });

        workOrder.releaseBay();
        WorkOrder saved = workOrderRepository.save(workOrder);
        return Result.success(saved);
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(StartWorkOrderCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder workOrder = orderOpt.get();
        try {
            workOrder.startWork();
            WorkOrder saved = workOrderRepository.save(workOrder);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(AddTaskToWorkOrderCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        if (serviceRepository.findById(command.serviceId()).isEmpty()) {
            return Result.failure(ApplicationError.notFound("Service with identifier " + command.serviceId().value() + " was not found"));
        }

        WorkOrder workOrder = orderOpt.get();
        try {
            workOrder.addTask(
                    command.serviceId(),
                    command.mechanicId(),
                    command.description(),
                    Money.soles(command.price()),
                    LaborHours.of(command.estimatedHours())
            );
            WorkOrder saved = workOrderRepository.save(workOrder);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(AssignTaskMechanicCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        if (tenancyAclService != null && !tenancyAclService.isMechanicEligible(command.mechanicId(), order.getTenantId().value())) {
            return Result.failure(ApplicationError.unprocessableEntity("Mechanic is not eligible or active in this tenant"));
        }
        try {
            order.assignTaskMechanic(command.taskId(), command.mechanicId());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(StartWorkOrderTaskCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.startTask(command.taskId());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(HoldWorkOrderTaskCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.holdTask(command.taskId(), command.missingItemDescription(), command.inventoryItemId());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(ResumeWorkOrderTaskCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.resumeTask(command.taskId());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(CompleteWorkOrderTaskCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.completeTask(command.taskId(), LaborHours.of(command.actualHours()));
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(ReopenWorkOrderTaskCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.reopenTask(command.taskId(), command.reason());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(AddProductToTaskCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.addProductToTask(command.taskId(), command.productId(), Quantity.of(command.quantity()), Money.soles(command.unitPrice()));
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(UpdateTaskProductQuantityCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        Optional<WorkOrderTask> taskOpt = order.getTasks().stream().filter(t -> t.getId().equals(command.taskId())).findFirst();
        if (taskOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        taskOpt.get().getConsumedProducts().stream()
                .filter(p -> p.getProductId().equals(command.productId()))
                .findFirst()
                .ifPresent(p -> p.updateQuantity(Quantity.of(command.newQuantity())));
        order.recalculateTotalAmount();
        WorkOrder saved = workOrderRepository.save(order);
        return Result.success(saved);
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(RemoveProductFromTaskCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.removeProductFromTask(command.taskId(), command.productItemId());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(AttachIntakeImageCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        order.attachIntakeImage(StorageUrl.of(command.imageUrl()), command.description());
        WorkOrder saved = workOrderRepository.save(order);
        return Result.success(saved);
    }

    @Override
    public Result<WorkOrderTask, ApplicationError> handle(AttachTaskEvidenceImageCommand command) {
        if (command == null || command.taskId() == null) {
            return Result.failure(ApplicationError.badRequest("Command and taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            order.attachTaskEvidence(command.taskId(), StorageUrl.of(command.imageUrl()), command.evidenceType(), command.description());
            workOrderRepository.save(order);
            WorkOrderTask task = order.getTasks().stream().filter(t -> t.getId().equals(command.taskId())).findFirst().orElseThrow();
            return Result.success(task);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<TaskProposal, ApplicationError> handle(SubmitTaskProposalCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        com.andeva.atelier.platform.operations.domain.model.ids.ServiceId suggestedId = command.suggestedServiceId() != null
                ? com.andeva.atelier.platform.operations.domain.model.ids.ServiceId.of(command.suggestedServiceId())
                : null;

        TaskProposal proposal = order.submitProposal(
                command.mechanicId(),
                command.description(),
                command.severity(),
                command.imageUrl(),
                suggestedId
        );
        workOrderRepository.save(order);
        return Result.success(proposal);
    }

    @Override
    public Result<WorkOrderTask, ApplicationError> handle(ApproveTaskProposalCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        try {
            WorkOrderTask task = order.approveProposal(
                    command.proposalId(),
                    command.serviceId(),
                    Money.soles(command.finalPrice()),
                    LaborHours.of(command.laborHours()),
                    command.mechanicId(),
                    command.notes()
            );
            workOrderRepository.save(order);
            return Result.success(task);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<TaskProposal, ApplicationError> handle(RejectTaskProposalCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        try {
            order.rejectProposal(command.proposalId(), command.customerNotes());
            workOrderRepository.save(order);
            Optional<TaskProposal> propOpt = order.getProposals().stream().filter(p -> p.getId().equals(command.proposalId())).findFirst();
            return propOpt.map(Result::<TaskProposal, ApplicationError>success)
                    .orElseGet(() -> Result.failure(ApplicationError.notFound("Task proposal not found")));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(MarkWorkOrderAsPaidCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        try {
            order.markPaid();
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(DeliverVehicleCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        try {
            order.deliverVehicle();
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(CancelWorkOrderCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        try {
            order.getCurrentBayId().ifPresent(bayId -> {
                workBayRepository.findById(bayId).ifPresent(bay -> {
                    bay.release();
                    workBayRepository.save(bay);
                });
            });
            order.cancel(command.reason());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(CompleteWorkOrderCommand command) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(command.workOrderId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder with identifier " + command.workOrderId().value() + " was not found"));
        }

        WorkOrder order = orderOpt.get();
        try {
            order.getCurrentBayId().ifPresent(bayId -> {
                workBayRepository.findById(bayId).ifPresent(bay -> {
                    bay.release();
                    workBayRepository.save(bay);
                });
            });
            order.completeOrder();
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }
}
