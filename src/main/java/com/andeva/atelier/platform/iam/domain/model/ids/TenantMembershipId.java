package com.andeva.atelier.platform.iam.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a Workshop Staff Membership contract.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantMembershipId(UUID value) implements Serializable {

    public TenantMembershipId {
        Objects.requireNonNull(value, "Tenant membership identifier cannot be null");
    }

    public static TenantMembershipId of(UUID value) {
        return new TenantMembershipId(value);
    }

    public static TenantMembershipId of(String value) {
        Objects.requireNonNull(value, "Tenant membership identifier string cannot be null");
        return new TenantMembershipId(UUID.fromString(value));
    }

    public static TenantMembershipId generate() {
        return new TenantMembershipId(UUID.randomUUID());
    }
}
