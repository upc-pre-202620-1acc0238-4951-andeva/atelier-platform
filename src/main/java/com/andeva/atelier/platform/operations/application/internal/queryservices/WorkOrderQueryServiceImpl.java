package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;
import com.andeva.atelier.platform.operations.application.queryservices.AssignedTaskView;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.queries.*;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkOrderQueryServiceImpl implements WorkOrderQueryService {

    private final WorkOrderRepository workOrderRepository;
    private final TenancyAclService tenancyAclService;

    public WorkOrderQueryServiceImpl(WorkOrderRepository workOrderRepository, TenancyAclService tenancyAclService) {
        this.workOrderRepository = Objects.requireNonNull(workOrderRepository);
        this.tenancyAclService = Objects.requireNonNull(tenancyAclService);
    }

    @Override
    public Optional<WorkOrder> handle(GetWorkOrderByIdQuery query) {
        if (query == null || query.workOrderId() == null) return Optional.empty();
        return workOrderRepository.findById(query.workOrderId());
    }

    @Override
    public List<WorkOrder> handle(GetWorkOrdersByTenantIdQuery query) {
        if (query == null || query.tenantId() == null) return List.of();
        return workOrderRepository.findByTenantId(query.tenantId());
    }

    @Override
    public List<WorkOrder> handle(GetWorkOrdersByBranchIdQuery query) {
        if (query == null || query.tenantId() == null || query.branchId() == null) return List.of();
        return workOrderRepository.findByBranchId(query.tenantId(), query.branchId());
    }

    @Override
    public List<WorkOrder> handle(GetWorkOrdersByVehicleIdQuery query) {
        if (query == null || query.vehicleId() == null) return List.of();
        return workOrderRepository.findByVehicleId(query.vehicleId());
    }

    @Override
    public List<WorkOrder> handle(GetWorkOrdersByCustomerIdQuery query) {
        if (query == null || query.customerId() == null) return List.of();
        return workOrderRepository.findByCustomerId(query.customerId());
    }

    @Override
    public Optional<WorkOrder> handle(GetWorkOrdersByBayIdQuery query) {
        if (query == null || query.bayId() == null) return Optional.empty();
        return workOrderRepository.findByCurrentBayId(query.bayId());
    }

    @Override
    public List<TaskProposal> handle(GetTaskProposalsByWorkOrderIdQuery query) {
        if (query == null || query.workOrderId() == null) return List.of();
        return workOrderRepository.findById(query.workOrderId())
                .map(WorkOrder::getProposals)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<AssignedTaskView> handle(GetTasksAssignedToMechanicQuery query) {
        if (query == null) return List.of();
        // Tasks are assigned to the staff membership, so the user is resolved to it first
        Optional<UUID> membershipId = tenancyAclService.resolveActiveMembershipId(query.mechanicUserId(), query.tenantId().value());
        if (membershipId.isEmpty()) return List.of();

        return workOrderRepository.findActiveWithTasksAssignedTo(query.tenantId(), membershipId.get()).stream()
                .flatMap(order -> order.getTasks().stream()
                        .filter(task -> task.getMechanicId().filter(membershipId.get()::equals).isPresent())
                        .filter(task -> task.getStatus() != WorkOrderTaskStatus.COMPLETED && task.getStatus() != WorkOrderTaskStatus.CANCELLED)
                        .map(task -> new AssignedTaskView(order, task)))
                .sorted(Comparator.comparingInt((AssignedTaskView view) -> priorityOf(view.task().getStatus()))
                        .thenComparing(view -> view.workOrder().getInternalNumber().sequence()))
                .toList();
    }

    // Work already started comes first, then paused work, then what is still to begin
    private static int priorityOf(WorkOrderTaskStatus status) {
        return switch (status) {
            case IN_PROGRESS -> 0;
            case ON_HOLD -> 1;
            case ASSIGNED -> 2;
            default -> 3;
        };
    }

    @Override
    public Optional<WorkOrderTask> handle(GetWorkOrderTaskByIdQuery query) {
        if (query == null || query.taskId() == null) return Optional.empty();
        return workOrderRepository.findByTaskId(query.taskId())
                .flatMap(w -> w.getTasks().stream()
                        .filter(t -> t.getId().equals(query.taskId()))
                        .findFirst());
    }
}
