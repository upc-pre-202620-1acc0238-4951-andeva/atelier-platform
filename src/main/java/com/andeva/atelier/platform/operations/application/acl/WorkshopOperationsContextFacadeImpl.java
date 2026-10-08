package com.andeva.atelier.platform.operations.application.acl;

import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.MarkWorkOrderAsPaidCommand;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.andeva.atelier.platform.operations.interfaces.acl.WorkshopOperationsContextFacade;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkBaySummaryDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderBillingDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderConsumedProductDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderSummaryDto;
import com.andeva.atelier.platform.operations.application.queryservices.ServiceQueryService;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServicesByTenantIdQuery;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkshopServiceCatalogAclDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkshopOperationsContextFacadeImpl implements WorkshopOperationsContextFacade {

    private final WorkOrderQueryService workOrderQueryService;
    private final WorkBayQueryService workBayQueryService;
    private final WorkOrderCommandService workOrderCommandService;
    private final ServiceQueryService serviceQueryService;

    public WorkshopOperationsContextFacadeImpl(
            WorkOrderQueryService workOrderQueryService,
            WorkBayQueryService workBayQueryService,
            WorkOrderCommandService workOrderCommandService) {
        this(workOrderQueryService, workBayQueryService, workOrderCommandService, null);
    }

    @Autowired
    public WorkshopOperationsContextFacadeImpl(
            WorkOrderQueryService workOrderQueryService,
            WorkBayQueryService workBayQueryService,
            WorkOrderCommandService workOrderCommandService,
            @Autowired(required = false) ServiceQueryService serviceQueryService) {
        this.workOrderQueryService = workOrderQueryService;
        this.workBayQueryService = workBayQueryService;
        this.workOrderCommandService = workOrderCommandService;
        this.serviceQueryService = serviceQueryService;
    }

    @Override
    public Optional<WorkOrderSummaryDto> fetchWorkOrderById(UUID workOrderId) {
        if (workOrderId == null) {
            return Optional.empty();
        }
        return workOrderQueryService.handle(new GetWorkOrderByIdQuery(new WorkOrderId(workOrderId)))
                .map(wo -> new WorkOrderSummaryDto(
                        wo.getId().value(),
                        wo.getTenantId().value(),
                        wo.getInternalNumber() != null ? wo.getInternalNumber().sequence() : null,
                        wo.getVehicleId().value(),
                        wo.getStatus().name(),
                        wo.getTotalAmount() != null ? wo.getTotalAmount().amount() : BigDecimal.ZERO,
                        wo.getTotalAmount() != null && wo.getTotalAmount().currency() != null
                                ? wo.getTotalAmount().currency().name()
                                : "PEN"
                ));
    }

    @Override
    public Optional<WorkOrderBillingDto> fetchWorkOrderBillingDetails(UUID workOrderId) {
        if (workOrderId == null) {
            return Optional.empty();
        }
        return workOrderQueryService.handle(new GetWorkOrderByIdQuery(new WorkOrderId(workOrderId)))
                .map(wo -> {
                    BigDecimal labor = BigDecimal.ZERO;
                    BigDecimal products = BigDecimal.ZERO;
                    if (wo.getTasks() != null) {
                        for (var task : wo.getTasks()) {
                            if (task.getPrice() != null && task.getPrice().amount() != null) {
                                labor = labor.add(task.getPrice().amount());
                            }
                            if (task.getConsumedProducts() != null) {
                                for (var prod : task.getConsumedProducts()) {
                                    if (prod.getTotalAmount() != null && prod.getTotalAmount().amount() != null) {
                                        products = products.add(prod.getTotalAmount().amount());
                                    }
                                }
                            }
                        }
                    }
                    return new WorkOrderBillingDto(
                            wo.getId().value(),
                            wo.getTenantId().value(),
                            wo.getInternalNumber() != null ? wo.getInternalNumber().sequence() : null,
                            wo.getCustomerId().value(),
                            wo.getVehicleId().value(),
                            labor,
                            products,
                            wo.getTotalAmount() != null ? wo.getTotalAmount().amount() : BigDecimal.ZERO,
                            wo.getTotalAmount() != null && wo.getTotalAmount().currency() != null
                                    ? wo.getTotalAmount().currency().name()
                                    : "PEN"
                    );
                });
    }

    @Override
    public List<WorkOrderConsumedProductDto> fetchProductsConsumedInOrder(UUID workOrderId) {
        if (workOrderId == null) {
            return List.of();
        }
        Optional<WorkOrder> optionalWo = workOrderQueryService.handle(new GetWorkOrderByIdQuery(new WorkOrderId(workOrderId)));
        if (optionalWo.isEmpty()) {
            return List.of();
        }
        WorkOrder wo = optionalWo.get();
        List<WorkOrderConsumedProductDto> list = new ArrayList<>();
        if (wo.getTasks() != null) {
            for (var task : wo.getTasks()) {
                if (task.getConsumedProducts() != null) {
                    for (var prod : task.getConsumedProducts()) {
                        list.add(new WorkOrderConsumedProductDto(
                                prod.getProductId(),
                                "Product " + prod.getProductId(),
                                prod.getQuantity() != null ? prod.getQuantity().value() : BigDecimal.ZERO,
                                prod.getUnitPrice() != null ? prod.getUnitPrice().amount() : BigDecimal.ZERO,
                                prod.getTotalAmount() != null ? prod.getTotalAmount().amount() : BigDecimal.ZERO,
                                prod.getUnitPrice() != null && prod.getUnitPrice().currency() != null
                                        ? prod.getUnitPrice().currency().name()
                                        : "PEN"
                        ));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean markWorkOrderAsPaid(UUID workOrderId) {
        if (workOrderId == null) {
            return false;
        }
        var result = workOrderCommandService.handle(new MarkWorkOrderAsPaidCommand(new WorkOrderId(workOrderId)));
        return result.isSuccess();
    }

    @Override
    public boolean isBayOccupied(UUID bayId) {
        if (bayId == null) {
            return false;
        }
        return workBayQueryService.getById(new WorkBayId(bayId))
                .map(bay -> bay.getStatus() == BayStatus.OCCUPIED)
                .orElse(false);
    }

    @Override
    public Optional<WorkBaySummaryDto> fetchBayStatus(UUID bayId) {
        if (bayId == null) {
            return Optional.empty();
        }
        return workBayQueryService.getById(new WorkBayId(bayId))
                .map(bay -> new WorkBaySummaryDto(
                        bay.getId().value(),
                        bay.getBranchId().value(),
                        bay.getName(),
                        bay.getType() != null ? bay.getType().name() : null,
                        bay.getStatus() != null ? bay.getStatus().name() : null,
                        bay.getCurrentWorkOrderId().map(WorkOrderId::value).orElse(null)
                ));
    }

    @Override
    public List<WorkshopServiceCatalogAclDto> fetchAvailableServices(UUID tenantId) {
        if (serviceQueryService == null || tenantId == null) {
            return List.of();
        }
        try {
            return serviceQueryService.handle(new GetServicesByTenantIdQuery(TenantId.of(tenantId)))
                    .stream()
                    .map(s -> new WorkshopServiceCatalogAclDto(
                            s.getId().value(),
                            s.getName(),
                            s.getBasePrice() != null ? s.getBasePrice().amount() : BigDecimal.ZERO,
                            s.getEstimatedDurationMinutes()
                    ))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
