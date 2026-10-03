package com.andeva.atelier.platform.shared.domain.model.valueobjects;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for the User Account.
 *
 * @author Joel Huamani Estefanero
 */
public record UserId(UUID value) implements Serializable {
    public UserId {
        Objects.requireNonNull(value, "User account identifier cannot be null");
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId of(String value) {
        Objects.requireNonNull(value, "User account identifier cannot be null");
        return new UserId(UUID.fromString(value));
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}
