package com.andeva.atelier.platform.iam.domain.model.entities;

import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;

import java.util.Objects;

/**
 * Atomic authorization privilege catalog entity representing a discrete operation within the system.
 *
 * @author Joel Huamani Estefanero
 */
public class Permission {

    private final PermissionId id;
    private final String name;
    private final String description;
    private final String category;

    public Permission(PermissionId id, String name, String description, String category) {
        this.id = Objects.requireNonNull(id, "Permission identifier cannot be null");
        this.name = validateName(name);
        this.description = Objects.requireNonNull(description, "Permission description cannot be null").trim();
        this.category = Objects.requireNonNull(category, "Permission category cannot be null").trim().toUpperCase();
    }

    public static Permission of(String name, String description, String category) {
        return new Permission(PermissionId.generate(), name, description, category);
    }

    public static Permission create(String name, String description, String category) {
        return of(name, description, category);
    }

    public static Permission of(PermissionId id, String name, String description, String category) {
        return new Permission(id, name, description, category);
    }

    public PermissionId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String category() {
        return category;
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "Permission name cannot be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
