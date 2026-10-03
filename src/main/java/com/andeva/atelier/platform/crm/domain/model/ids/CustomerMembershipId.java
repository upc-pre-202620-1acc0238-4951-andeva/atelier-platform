package com.andeva.atelier.platform.crm.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record CustomerMembershipId(UUID value) implements Serializable {

    public CustomerMembershipId {
        Objects.requireNonNull(value, "CustomerMembershipId value cannot be null");
    }

    public static CustomerMembershipId generate() {
        return new CustomerMembershipId(UUID.randomUUID());
    }

    public static CustomerMembershipId of(UUID value) {
        return new CustomerMembershipId(value);
    }
}
