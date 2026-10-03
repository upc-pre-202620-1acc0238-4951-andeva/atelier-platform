package com.andeva.atelier.platform.shared.domain.model.valueobjects;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for the physical Workshop Branch.
 *
 * @author Joel Huamani Estefanero
 */
public record BranchId(UUID value) implements Serializable {
    public BranchId {
        Objects.requireNonNull(value, "Branch identifier cannot be null");
    }

    public static BranchId of(UUID value) {
        return new BranchId(value);
    }

    public static BranchId of(String value) {
        Objects.requireNonNull(value, "Branch identifier cannot be null");
        return new BranchId(UUID.fromString(value));
    }

    public static BranchId generate() {
        return new BranchId(UUID.randomUUID());
    }
}
