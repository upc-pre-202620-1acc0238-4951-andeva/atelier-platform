package com.andeva.atelier.platform.iam.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for an Atomic Authorization Permission.
 *
 * @author Joel Huamani Estefanero
 */
public record PermissionId(UUID value) implements Serializable {

    public PermissionId {
        Objects.requireNonNull(value, "Permission identifier cannot be null");
    }

    public static PermissionId of(UUID value) {
        return new PermissionId(value);
    }

    public static PermissionId of(String value) {
        Objects.requireNonNull(value, "Permission identifier string cannot be null");
        return new PermissionId(UUID.fromString(value));
    }

    public static PermissionId generate() {
        return new PermissionId(UUID.randomUUID());
    }
}
