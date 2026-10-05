package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.RegisterCompanyCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateCompanyCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateIndividualCustomerResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.UUID;

/**
 * Assembler creating customer registration commands from REST resources.
 *
 * @author Adiel Sanchez Santin
 */
public final class RegisterCustomerCommandFromResourceAssembler {

    private RegisterCustomerCommandFromResourceAssembler() {
    }

    public static RegisterIndividualCustomerCommand toCommandFromResource(UUID tenantId, CreateIndividualCustomerResource resource) {
        return new RegisterIndividualCustomerCommand(
                TenantId.of(tenantId),
                resource.firstName().trim(),
                resource.lastName().trim(),
                resource.taxId().trim(),
                resource.email() != null ? resource.email().trim() : null,
                resource.phone() != null ? resource.phone().trim() : null
        );
    }

    public static RegisterCompanyCustomerCommand toCommandFromResource(UUID tenantId, CreateCompanyCustomerResource resource) {
        return new RegisterCompanyCustomerCommand(
                TenantId.of(tenantId),
                resource.companyName().trim(),
                resource.taxId().trim(),
                resource.email() != null ? resource.email().trim() : null,
                resource.phone() != null ? resource.phone().trim() : null
        );
    }
}
