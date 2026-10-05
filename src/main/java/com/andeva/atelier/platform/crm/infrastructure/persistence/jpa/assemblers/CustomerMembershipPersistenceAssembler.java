package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.CustomerMembershipPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

public final class CustomerMembershipPersistenceAssembler {

    private CustomerMembershipPersistenceAssembler() {
    }

    public static CustomerMembership toDomain(CustomerMembershipPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CustomerMembership(
                CustomerMembershipId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                UserId.of(entity.getUserId()),
                entity.getRole(),
                entity.getStatus()
        );
    }

    public static CustomerMembershipPersistenceEntity toEntity(CustomerMembership domain) {
        if (domain == null) {
            return null;
        }

        return new CustomerMembershipPersistenceEntity(
                domain.getId().value(),
                domain.getCustomerId().value(),
                domain.getUserId().value(),
                domain.getRole(),
                domain.getStatus()
        );
    }
}
