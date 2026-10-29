package com.andeva.atelier.platform.operations.application.internal.commandservices;

import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.*;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerFleetAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryItemSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderSettlementAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkforceShiftAclService;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class WorkOrderCommandServiceImpl implements WorkOrderCommandService {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final ZoneId WORKSHOP_ZONE = ZoneId.of("America/Lima");

    private final WorkOrderRepository workOrderRepository;
    private final WorkBayRepository workBayRepository;
    private final ServiceRepository serviceRepository;
    private final DirectToCloudStorageGateway storageGateway;
    private final CustomerFleetAclService customerFleetAclService;
    private final TenancyAclService tenancyAclService;
    private final InventoryReservationAclService inventoryAclService;
    private final WorkforceShiftAclService workforceShiftAclService;
    private final WorkOrderSettlementAclService settlementAclService;

    public WorkOrderCommandServiceImpl(
            WorkOrderRepository workOrderRepository,
            WorkBayRepository workBayRepository,
            ServiceRepository serviceRepository,
            DirectToCloudStorageGateway storageGateway
    ) {
        this(workOrderRepository, workBayRepository, serviceRepository, storageGateway, null, null, null, null);
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
        this(workOrderRepository, workBayRepository, serviceRepository, storageGateway, customerFleetAclService,
                tenancyAclService, inventoryAclService, null);
    }

    public WorkOrderCommandServiceImpl(
            WorkOrderRepository workOrderRepository,
            WorkBayRepository workBayRepository,
            ServiceRepository serviceRepository,
            DirectToCloudStorageGateway storageGateway,
            CustomerFleetAclService customerFleetAclService,
            TenancyAclService tenancyAclService,
            InventoryReservationAclService inventoryAclService,
            WorkforceShiftAclService workforceShiftAclService
    ) {
        this(workOrderRepository, workBayRepository, serviceRepository, storageGateway, customerFleetAclService,
                tenancyAclService, inventoryAclService, workforceShiftAclService, null);
    }

    @Autowired
    public WorkOrderCommandServiceImpl(
            WorkOrderRepository workOrderRepository,
            WorkBayRepository workBayRepository,
            ServiceRepository serviceRepository,
            DirectToCloudStorageGateway storageGateway,
            CustomerFleetAclService customerFleetAclService,
            TenancyAclService tenancyAclService,
            InventoryReservationAclService inventoryAclService,
            WorkforceShiftAclService workforceShiftAclService,
            WorkOrderSettlementAclService settlementAclService
    ) {
        this.settlementAclService = settlementAclService;
        this.workOrderRepository = Objects.requireNonNull(workOrderRepository);
        this.workBayRepository = Objects.requireNonNull(workBayRepository);
        this.serviceRepository = Objects.requireNonNull(serviceRepository);
        this.storageGateway = Objects.requireNonNull(storageGateway);
        this.customerFleetAclService = customerFleetAclService;
        this.tenancyAclService = tenancyAclService;
        this.inventoryAclService = inventoryAclService;
        this.workforceShiftAclService = workforceShiftAclService;
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(CreateWorkOrderCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.badRequest("CreateWorkOrderCommand cannot be null"));
        }

        // A vehicle stays in custody until its order is paid or canceled: only one open order at a time
        Optional<WorkOrder> openOrder = workOrderRepository.findOpenByVehicleId(command.tenantId(), command.vehicleId());
        if (openOrder.isPresent()) {
            return Result.failure(ApplicationError.conflict("Vehicle already has an open work order #"
                    + openOrder.get().getInternalNumber().sequence() + " (" + openOrder.get().getStatus() + "): "
                    + openOrder.get().getId().value()));
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

        Optional<com.andeva.atelier.platform.operations.domain.model.aggregates.Service> serviceOpt = serviceRepository.findById(command.serviceId());
        if (serviceOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Service with identifier " + command.serviceId().value() + " was not found"));
        }

        // Labor price and estimated hours default to the catalog service when not overridden
        var service = serviceOpt.get();
        BigDecimal price = command.price() != null ? command.price() : service.getBasePrice().amount();
        BigDecimal estimatedHours = command.estimatedHours() != null
                ? command.estimatedHours()
                : BigDecimal.valueOf(Math.max(service.getEstimatedDurationMinutes(), 1)).divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);

        WorkOrder workOrder = orderOpt.get();
        try {
            workOrder.addTask(
                    command.serviceId(),
                    command.mechanicId(),
                    command.description(),
                    Money.soles(price),
                    LaborHours.of(estimatedHours)
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
        UUID requestedBy = command.requestedByUserId() != null && tenancyAclService != null
                ? tenancyAclService.resolveActiveMembershipId(command.requestedByUserId(), order.getTenantId().value()).orElse(null)
                : null;
        try {
            // The head mechanic's request waits for administrative approval; the task keeps running meanwhile
            order.requestTaskSuspension(command.taskId(), command.missingItemDescription(), requestedBy);
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(ApproveTaskSuspensionCommand command) {
        return applyToTask(command == null ? null : command.taskId(), (order, taskId) -> order.approveTaskSuspension(taskId));
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(RejectTaskSuspensionCommand command) {
        return applyToTask(command == null ? null : command.taskId(), (order, taskId) -> order.rejectTaskSuspension(taskId));
    }

    @Override
    public Result<WorkOrder, ApplicationError> handle(PauseWorkOrderTaskCommand command) {
        if (command == null || command.taskId() == null || command.reason() == null) {
            return Result.failure(ApplicationError.badRequest("taskId and reason are required"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(command.taskId());
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + command.taskId().value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();

        // An end-of-shift pause is refused while the mechanic's scheduled shift is still running
        if (command.reason() == HoldReason.END_OF_SHIFT && workforceShiftAclService != null) {
            Optional<UUID> mechanicId = order.getTasks().stream()
                    .filter(t -> t.getId().equals(command.taskId()))
                    .findFirst()
                    .flatMap(WorkOrderTask::getMechanicId);
            Optional<WorkforceShiftAclService.ShiftWindow> shift = mechanicId
                    .flatMap(id -> workforceShiftAclService.findShift(order.getTenantId().value(), id));
            LocalTime now = LocalTime.now(WORKSHOP_ZONE);
            if (shift.isPresent() && !shift.get().end().isBefore(shift.get().start()) && now.isBefore(shift.get().end())) {
                return Result.failure(ApplicationError.unprocessableEntity("The shift is still running until "
                        + shift.get().end() + ": pause for a break instead of the end of the shift"));
            }
        }

        try {
            order.pauseTask(command.taskId(), command.reason());
            return Result.success(workOrderRepository.save(order));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
    }

    @Override
    public int handle(PauseTasksOnClockOutCommand command) {
        int paused = 0;
        for (WorkOrder order : workOrderRepository.findActiveWithTasksAssignedTo(command.tenantId(), command.mechanicMembershipId())) {
            List<WorkOrderTaskId> running = order.getTasks().stream()
                    .filter(t -> t.getStatus() == WorkOrderTaskStatus.IN_PROGRESS)
                    .filter(t -> t.getMechanicId().filter(command.mechanicMembershipId()::equals).isPresent())
                    .map(WorkOrderTask::getId)
                    .toList();
            if (running.isEmpty()) {
                continue;
            }
            running.forEach(taskId -> order.pauseTask(taskId, HoldReason.END_OF_SHIFT));
            workOrderRepository.save(order);
            paused += running.size();
        }
        return paused;
    }

    private Result<WorkOrder, ApplicationError> applyToTask(WorkOrderTaskId taskId, java.util.function.BiConsumer<WorkOrder, WorkOrderTaskId> change) {
        if (taskId == null) {
            return Result.failure(ApplicationError.badRequest("taskId cannot be null"));
        }
        Optional<WorkOrder> orderOpt = workOrderRepository.findByTaskId(taskId);
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Task with identifier " + taskId.value() + " was not found"));
        }
        WorkOrder order = orderOpt.get();
        try {
            change.accept(order, taskId);
            return Result.success(workOrderRepository.save(order));
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
        BigDecimal unitPrice = command.unitPrice();
        if (inventoryAclService != null) {
            UUID tenantId = order.getTenantId().value();
            Optional<InventoryItemSummaryDto> item = inventoryAclService.fetchItemDetails(tenantId, command.productId());
            if (item.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Inventory item with identifier " + command.productId() + " was not found"));
            }
            // Reject up front: the FIFO discount runs after commit and could no longer refuse the line
            if (!inventoryAclService.checkItemStockAvailability(tenantId, command.productId(), command.quantity())) {
                return Result.failure(ApplicationError.conflict("Insufficient stock for " + item.get().sku() + ": requested "
                        + command.quantity().stripTrailingZeros().toPlainString() + ", available "
                        + item.get().currentStock().stripTrailingZeros().toPlainString()));
            }
            if (unitPrice == null) {
                unitPrice = item.get().basePrice();
            }
        }
        if (unitPrice == null) {
            return Result.failure(ApplicationError.badRequest("unitPrice is required when the catalog price cannot be resolved"));
        }
        try {
            order.addProductToTask(command.taskId(), command.productId(), Quantity.of(command.quantity()), Money.soles(unitPrice));
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
        if (command.newQuantity() == null || command.newQuantity().signum() <= 0) {
            return Result.failure(ApplicationError.badRequest("newQuantity must be strictly positive"));
        }
        WorkOrder order = orderOpt.get();
        Optional<WorkOrderTaskProduct> line = order.findTaskProduct(command.taskId(), command.productId());
        if (line.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Product " + command.productId() + " was not found in task " + command.taskId().value()));
        }
        BigDecimal extra = command.newQuantity().subtract(line.get().getQuantity().value());
        if (extra.signum() > 0 && inventoryAclService != null) {
            UUID tenantId = order.getTenantId().value();
            UUID productId = line.get().getProductId();
            // Same up-front check as adding a part: the FIFO allocation of the extra units runs after commit
            if (!inventoryAclService.checkItemStockAvailability(tenantId, productId, extra)) {
                String available = inventoryAclService.fetchItemDetails(tenantId, productId)
                        .map(item -> item.currentStock().stripTrailingZeros().toPlainString())
                        .orElse("0");
                return Result.failure(ApplicationError.conflict("Insufficient stock: requested "
                        + extra.stripTrailingZeros().toPlainString() + " more, available " + available));
            }
        }
        try {
            order.updateProductQuantityInTask(command.taskId(), command.productId(), Quantity.of(command.newQuantity()), command.changedBy());
            WorkOrder saved = workOrderRepository.save(order);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unprocessableEntity(e.getMessage()));
        }
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
            order.removeProductFromTask(command.taskId(), command.productItemId(), command.removedBy());
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

        if (!storageGateway.validateStorageUrl(command.imageUrl())) {
            return Result.failure(ApplicationError.badRequest("Image URL must point to the platform storage bucket"));
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
        if (!storageGateway.validateStorageUrl(command.imageUrl())) {
            return Result.failure(ApplicationError.badRequest("Image URL must point to the platform storage bucket"));
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

        if (command.imageUrl() != null && !storageGateway.validateStorageUrl(command.imageUrl().value())) {
            return Result.failure(ApplicationError.badRequest("Image URL must point to the platform storage bucket"));
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
        // US39 E2/E3: the order is paid only once the vouchers that bill it are fully paid
        if (settlementAclService != null && !settlementAclService.isSettled(order.getId().value())) {
            return Result.failure(ApplicationError.unprocessableEntity(
                    "The work order cannot be marked as paid: its electronic voucher has a pending balance or was not issued"));
        }
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
