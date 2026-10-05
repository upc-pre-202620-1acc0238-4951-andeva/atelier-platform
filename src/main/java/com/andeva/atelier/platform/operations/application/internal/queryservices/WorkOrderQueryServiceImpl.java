package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.queries.*;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkOrderQueryServiceImpl implements WorkOrderQueryService {

    private final WorkOrderRepository workOrderRepository;

    public WorkOrderQueryServiceImpl(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = Objects.requireNonNull(workOrderRepository);
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
    public Optional<WorkOrderTask> handle(GetWorkOrderTaskByIdQuery query) {
        if (query == null || query.taskId() == null) return Optional.empty();
        return workOrderRepository.findByTenantId(null).stream()
                .flatMap(w -> w.getTasks().stream())
                .filter(t -> t.getId().equals(query.taskId()))
                .findFirst();
    }
}
