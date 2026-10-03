package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record CustomerRegisteredEvent(
        CustomerId customerId,
        TenantId tenantId,
        CustomerType type,
        String displayName,
        TaxId taxId,
        Instant occurredOn
) implements Serializable {

    public CustomerRegisteredEvent {
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(type, "CustomerType cannot be null");
        Objects.requireNonNull(displayName, "DisplayName cannot be null");
        Objects.requireNonNull(taxId, "TaxId cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static CustomerRegisteredEvent of(CustomerId customerId, TenantId tenantId, CustomerType type, String displayName, TaxId taxId) {
        return new CustomerRegisteredEvent(customerId, tenantId, type, displayName, taxId, Instant.now());
    }
}
