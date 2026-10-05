package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.CustomerMembershipResource;

/**
 * Assembler transforming CustomerMembership entities into CustomerMembershipResource REST responses.
 *
 * @author Adiel Sanchez Santin
 */
public final class CustomerMembershipResourceFromEntityAssembler {

    private CustomerMembershipResourceFromEntityAssembler() {
    }

    public static CustomerMembershipResource toResourceFromEntity(CustomerMembership membership) {
        if (membership == null) {
            return null;
        }

        return new CustomerMembershipResource(
                membership.getId().value(),
                membership.getCustomerId().value(),
                membership.getUserId().value(),
                membership.getRole().name(),
                membership.getStatus().name()
        );
    }
}
