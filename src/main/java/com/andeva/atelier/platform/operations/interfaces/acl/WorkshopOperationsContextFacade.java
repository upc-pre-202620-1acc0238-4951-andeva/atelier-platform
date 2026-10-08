package com.andeva.atelier.platform.operations.interfaces.acl;

import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkBaySummaryDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderBillingDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderConsumedProductDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkOrderSummaryDto;
import com.andeva.atelier.platform.operations.interfaces.acl.dto.WorkshopServiceCatalogAclDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkshopOperationsContextFacade {
    Optional<WorkOrderSummaryDto> fetchWorkOrderById(UUID workOrderId);
    Optional<WorkOrderBillingDto> fetchWorkOrderBillingDetails(UUID workOrderId);
    List<WorkOrderConsumedProductDto> fetchProductsConsumedInOrder(UUID workOrderId);
    boolean markWorkOrderAsPaid(UUID workOrderId);
    boolean isBayOccupied(UUID bayId);
    Optional<WorkBaySummaryDto> fetchBayStatus(UUID bayId);
    List<WorkshopServiceCatalogAclDto> fetchAvailableServices(UUID tenantId);
}
