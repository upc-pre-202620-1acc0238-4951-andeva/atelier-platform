package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetAvailableWorkBaysQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkBaysByBranchIdQuery;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkBayQueryServiceImpl implements WorkBayQueryService {

    private final WorkBayRepository workBayRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkBayQueryServiceImpl(WorkBayRepository workBayRepository) {
        this(workBayRepository, null);
    }

    @Autowired
    public WorkBayQueryServiceImpl(WorkBayRepository workBayRepository, WorkOrderRepository workOrderRepository) {
        this.workBayRepository = Objects.requireNonNull(workBayRepository);
        this.workOrderRepository = workOrderRepository;
    }

    @Override
    public List<WorkBay> handle(GetWorkBaysByBranchIdQuery query) {
        if (query == null || query.tenantId() == null || query.branchId() == null) return List.of();
        List<WorkBay> bays = workBayRepository.findByTenantIdAndBranchId(query.tenantId(), query.branchId());
        if (workOrderRepository == null || bays.isEmpty()) return bays;

        Map<WorkBayId, WorkOrderId> activeOrderByBay = new HashMap<>();
        for (WorkOrder order : workOrderRepository.findByBranchId(query.tenantId(), query.branchId())) {
            if (!order.getStatus().isTerminal() && order.getStatus() != WorkOrderStatus.COMPLETED) {
                order.getCurrentBayId().ifPresent(bayId -> activeOrderByBay.putIfAbsent(bayId, order.getId()));
            }
        }
        return bays.stream()
                .map(bay -> reconcileBay(bay, activeOrderByBay.get(bay.getId())))
                .toList();
    }

    @Override
    public List<WorkBay> handle(GetAvailableWorkBaysQuery query) {
        if (query == null || query.tenantId() == null || query.branchId() == null) return List.of();
        return workBayRepository.findAvailableBays(query.tenantId(), query.branchId(), query.bayType());
    }

    @Override
    public Optional<WorkBay> getById(WorkBayId bayId) {
        if (bayId == null) return Optional.empty();
        return workBayRepository.findById(bayId).map(bay -> {
            if (workOrderRepository == null) return bay;
            WorkOrderId activeOrderId = workOrderRepository.findByCurrentBayId(bay.getId())
                    .filter(order -> !order.getStatus().isTerminal() && order.getStatus() != WorkOrderStatus.COMPLETED)
                    .map(WorkOrder::getId)
                    .orElse(null);
            return reconcileBay(bay, activeOrderId);
        });
    }

    private WorkBay reconcileBay(WorkBay bay, WorkOrderId activeOrderId) {
        if (activeOrderId != null) {
            return new WorkBay(bay.getId(), bay.getTenantId(), bay.getBranchId(), bay.getName(), bay.getType(),
                    BayStatus.OCCUPIED, activeOrderId);
        }
        if (bay.getStatus() == BayStatus.OCCUPIED && bay.getCurrentWorkOrderId().isEmpty()) {
            return new WorkBay(bay.getId(), bay.getTenantId(), bay.getBranchId(), bay.getName(), bay.getType(),
                    BayStatus.AVAILABLE, null);
        }
        return bay;
    }
}
