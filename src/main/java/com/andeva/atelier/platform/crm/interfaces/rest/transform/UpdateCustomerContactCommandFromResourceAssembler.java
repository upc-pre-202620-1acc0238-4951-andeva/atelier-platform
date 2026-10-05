package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.UpdateCustomerContactCommand;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.UpdateCustomerContactResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.UUID;

/**
 * Assembler creating UpdateCustomerContactCommand from REST resource.
 *
 * @author Adiel Sanchez Santin
 */
public final class UpdateCustomerContactCommandFromResourceAssembler {

    private UpdateCustomerContactCommandFromResourceAssembler() {
    }

    public static UpdateCustomerContactCommand toCommandFromResource(
            UUID tenantId,
            UUID customerId,
            UpdateCustomerContactResource resource
    ) {
        return new UpdateCustomerContactCommand(
                TenantId.of(tenantId),
                CustomerId.of(customerId),
                resource.email() != null ? resource.email().trim() : null,
                resource.phone() != null ? resource.phone().trim() : null
        );
    }
}
