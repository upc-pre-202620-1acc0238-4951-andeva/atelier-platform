package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkBayCommand;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CreateWorkBayResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.UUID;

public final class CreateWorkBayCommandFromResourceAssembler {
    private CreateWorkBayCommandFromResourceAssembler() {}

    public static CreateWorkBayCommand toCommandFromResource(UUID tenantId, CreateWorkBayResource resource) {
        BayType resolvedType = BayType.LIFT;
        if (resource.bayType() != null) {
            try {
                resolvedType = BayType.valueOf(resource.bayType());
            } catch (IllegalArgumentException e) {
                if ("MECHANICAL_LIFT".equalsIgnoreCase(resource.bayType())) {
                    resolvedType = BayType.LIFT;
                } else if ("WASH_BAY".equalsIgnoreCase(resource.bayType())) {
                    resolvedType = BayType.WASHING;
                } else if ("ALIGNMENT_STATION".equalsIgnoreCase(resource.bayType())) {
                    resolvedType = BayType.ALIGNMENT;
                }
            }
        }
        return new CreateWorkBayCommand(
                tenantId != null ? new TenantId(tenantId) : null,
                resource.branchId() != null ? new BranchId(resource.branchId()) : null,
                resource.name(),
                resolvedType
        );
    }
}
