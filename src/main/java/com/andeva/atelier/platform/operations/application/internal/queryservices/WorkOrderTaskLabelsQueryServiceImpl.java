package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderTaskLabelsQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class WorkOrderTaskLabelsQueryServiceImpl implements WorkOrderTaskLabelsQueryService {

    private final ServiceRepository serviceRepository;
    private final InventoryReservationAclService inventoryAclService;

    public WorkOrderTaskLabelsQueryServiceImpl(ServiceRepository serviceRepository, InventoryReservationAclService inventoryAclService) {
        this.serviceRepository = Objects.requireNonNull(serviceRepository, "serviceRepository cannot be null");
        this.inventoryAclService = Objects.requireNonNull(inventoryAclService, "inventoryAclService cannot be null");
    }

    @Override
    public String serviceNameOf(WorkOrderTask task) {
        if (task == null || task.getServiceId() == null) return SERVICE_FALLBACK;
        return serviceRepository.findById(task.getServiceId()).map(Service::getName).orElse(SERVICE_FALLBACK);
    }

    @Override
    public String productNameOf(UUID tenantId, UUID productId) {
        // Without the workshop the ACL answers a placeholder item, so the id is shown instead
        if (tenantId == null || productId == null) return WorkOrderTaskLabelsQueryService.productFallback(productId);
        return inventoryAclService.fetchItemDetails(tenantId, productId)
                .map(item -> item.name())
                .filter(name -> !name.isBlank())
                .orElseGet(() -> WorkOrderTaskLabelsQueryService.productFallback(productId));
    }
}
