package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.InviteCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.InviteCustomerMemberResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Locale;
import java.util.UUID;

/**
 * Assembler creating InviteCustomerMemberCommand from REST resource.
 *
 * @author Adiel Sanchez Santin
 */
public final class InviteCustomerMemberCommandFromResourceAssembler {

    private InviteCustomerMemberCommandFromResourceAssembler() {
    }

    public static InviteCustomerMemberCommand toCommandFromResource(
            UUID tenantId,
            UUID customerId,
            InviteCustomerMemberResource resource
    ) {
        FleetRole role = FleetRole.valueOf(resource.role().trim().toUpperCase(Locale.ROOT));

        return new InviteCustomerMemberCommand(
                TenantId.of(tenantId),
                CustomerId.of(customerId),
                UserId.of(resource.userId()),
                role
        );
    }
}
