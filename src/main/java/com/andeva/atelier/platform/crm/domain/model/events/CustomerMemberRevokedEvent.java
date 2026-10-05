package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record CustomerMemberRevokedEvent(
        CustomerMembershipId membershipId,
        CustomerId customerId,
        UserId userId,
        Instant occurredOn
) implements Serializable {

    public CustomerMemberRevokedEvent {
        Objects.requireNonNull(membershipId, "CustomerMembershipId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(userId, "UserId cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static CustomerMemberRevokedEvent of(CustomerMembershipId membershipId, CustomerId customerId, UserId userId) {
        return new CustomerMemberRevokedEvent(membershipId, customerId, userId, Instant.now());
    }
}
