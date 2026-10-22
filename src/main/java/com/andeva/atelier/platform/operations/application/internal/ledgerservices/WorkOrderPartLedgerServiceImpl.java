package com.andeva.atelier.platform.operations.application.internal.ledgerservices;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderPartLedgerAclService;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderPartLedgerAclService.PartCost;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderPartLedgerAclService.PartMovement;
import com.andeva.atelier.platform.operations.application.ledgerservices.WorkOrderPartLedgerService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkOrderPartLedgerServiceImpl implements WorkOrderPartLedgerService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderPartLedgerAclService ledgerAclService;

    public WorkOrderPartLedgerServiceImpl(WorkOrderRepository workOrderRepository, WorkOrderPartLedgerAclService ledgerAclService) {
        this.workOrderRepository = Objects.requireNonNull(workOrderRepository);
        this.ledgerAclService = Objects.requireNonNull(ledgerAclService);
    }

    @Override
    public Result<Profitability, ApplicationError> profitability(TenantId tenantId, WorkOrderId workOrderId) {
        Optional<WorkOrder> orderOpt = findOrder(tenantId, workOrderId);
        if (orderOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder", workOrderId.value()));
        }
        WorkOrder order = orderOpt.get();

        // FIFO cost still charged, per task and item
        Map<String, BigDecimal> costByLine = new HashMap<>();
        for (PartCost cost : ledgerAclService.costsOf(workOrderId.value())) {
            costByLine.merge(key(cost.taskId(), cost.itemId()), cost.totalCost(), BigDecimal::add);
        }

        BigDecimal labor = BigDecimal.ZERO;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        List<PartLine> lines = new ArrayList<>();
        for (WorkOrderTask task : order.getTasks()) {
            if (task.getStatus() == WorkOrderTaskStatus.CANCELLED) {
                continue;
            }
            labor = labor.add(task.getPrice().amount());
            Map<UUID, BigDecimal> revenueByItem = new HashMap<>();
            Map<UUID, BigDecimal> quantityByItem = new HashMap<>();
            Map<UUID, BigDecimal> priceByItem = new HashMap<>();
            for (WorkOrderTaskProduct product : task.getConsumedProducts()) {
                revenueByItem.merge(product.getProductId(), product.getTotalAmount().amount(), BigDecimal::add);
                quantityByItem.merge(product.getProductId(), product.getQuantity().value(), BigDecimal::add);
                priceByItem.putIfAbsent(product.getProductId(), product.getUnitPrice().amount());
            }
            for (Map.Entry<UUID, BigDecimal> entry : revenueByItem.entrySet()) {
                UUID itemId = entry.getKey();
                BigDecimal lineRevenue = scale(entry.getValue());
                BigDecimal lineCost = scale(costByLine.getOrDefault(key(task.getId().value(), itemId), BigDecimal.ZERO));
                lines.add(new PartLine(task.getId().value(), itemId, quantityByItem.get(itemId), priceByItem.get(itemId),
                        lineRevenue, lineCost, lineRevenue.subtract(lineCost)));
                revenue = revenue.add(lineRevenue);
                cost = cost.add(lineCost);
            }
        }

        return Result.success(new Profitability(order.getId().value(), scale(labor), scale(revenue), scale(cost),
                scale(revenue.subtract(cost)), scale(labor.add(revenue)), order.getSubtotal().currency().name(), lines));
    }

    @Override
    public Result<List<PartMovement>, ApplicationError> partMovements(TenantId tenantId, WorkOrderId workOrderId) {
        if (findOrder(tenantId, workOrderId).isEmpty()) {
            return Result.failure(ApplicationError.notFound("WorkOrder", workOrderId.value()));
        }
        return Result.success(ledgerAclService.movementsOf(workOrderId.value()));
    }

    private Optional<WorkOrder> findOrder(TenantId tenantId, WorkOrderId workOrderId) {
        return workOrderRepository.findById(workOrderId).filter(order -> order.getTenantId().equals(tenantId));
    }

    private static String key(UUID taskId, UUID itemId) {
        return taskId + "|" + itemId;
    }

    private static BigDecimal scale(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
