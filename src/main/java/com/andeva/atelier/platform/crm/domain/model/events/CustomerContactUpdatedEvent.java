package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record CustomerContactUpdatedEvent(
        CustomerId customerId,
        EmailAddress email,
        PhoneNumber phone,
        Instant occurredOn
) implements Serializable {

    public CustomerContactUpdatedEvent {
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static CustomerContactUpdatedEvent of(CustomerId customerId, EmailAddress email, PhoneNumber phone) {
        return new CustomerContactUpdatedEvent(customerId, email, phone, Instant.now());
    }
}
