package com.andeva.atelier.platform.iam.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a Workshop Security Role.
 *
 * @author Joel Huamani Estefanero
 */
public record RoleId(UUID value) implements Serializable {

    public RoleId {
        Objects.requireNonNull(value, "Role identifier cannot be null");
    }

    public static RoleId of(UUID value) {
        return new RoleId(value);
    }

    public static RoleId of(String value) {
        Objects.requireNonNull(value, "Role identifier string cannot be null");
        return new RoleId(UUID.fromString(value));
    }

    public static RoleId generate() {
        return new RoleId(UUID.randomUUID());
    }
}
