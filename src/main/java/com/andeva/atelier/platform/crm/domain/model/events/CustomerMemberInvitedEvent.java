package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record CustomerMemberInvitedEvent(
        CustomerMembershipId membershipId,
        CustomerId customerId,
        UserId userId,
        FleetRole role,
        Instant occurredOn
) implements Serializable {

    public CustomerMemberInvitedEvent {
        Objects.requireNonNull(membershipId, "CustomerMembershipId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(userId, "UserId cannot be null");
        Objects.requireNonNull(role, "FleetRole cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static CustomerMemberInvitedEvent of(CustomerMembershipId membershipId, CustomerId customerId, UserId userId, FleetRole role) {
        return new CustomerMemberInvitedEvent(membershipId, customerId, userId, role, Instant.now());
    }
}
