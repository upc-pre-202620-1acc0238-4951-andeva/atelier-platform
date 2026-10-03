package com.andeva.atelier.platform.shared.domain.model.valueobjects;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for the Workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantId(UUID value) implements Serializable {
    public TenantId {
        Objects.requireNonNull(value, "Workshop (Tenant) identifier cannot be null");
    }

    public static TenantId of(UUID value) {
        return new TenantId(value);
    }

    public static TenantId of(String value) {
        Objects.requireNonNull(value, "Workshop (Tenant) identifier cannot be null");
        return new TenantId(UUID.fromString(value));
    }

    public static TenantId generate() {
        return new TenantId(UUID.randomUUID());
    }
}
