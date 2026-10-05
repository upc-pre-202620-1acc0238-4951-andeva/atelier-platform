package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkBayResource;

public final class WorkBayResourceAssembler {
    private WorkBayResourceAssembler() {}

    public static WorkBayResource toResourceFromEntity(WorkBay entity) {
        if (entity == null) {
            return null;
        }
        return new WorkBayResource(
                entity.getId().value(),
                entity.getTenantId().value(),
                entity.getBranchId().value(),
                entity.getName(),
                entity.getType().name(),
                entity.getStatus().name(),
                entity.getCurrentWorkOrderId().map(WorkOrderId::value).orElse(null)
        );
    }
}
