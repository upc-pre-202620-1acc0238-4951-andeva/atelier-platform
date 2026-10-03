package com.andeva.atelier.platform.iam.domain.model.entities;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Dependent physical workshop branch location entity managed under the Tenant aggregate.
 *
 * @author Joel Huamani Estefanero
 */
public class Branch {

    private final BranchId id;
    private final TenantId tenantId;
    private String name;
    private String sunatCode;
    private GeoPoint location;
    private int geofenceRadiusMeters;
    private boolean active;

    public Branch(
            BranchId id,
            TenantId tenantId,
            String name,
            String sunatCode,
            GeoPoint location,
            int geofenceRadiusMeters,
            boolean active) {
        this.id = Objects.requireNonNull(id, "Branch identifier cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        this.name = validateName(name);
        this.sunatCode = validateSunatCode(sunatCode);
        this.location = Objects.requireNonNull(location, "GPS location cannot be null");
        this.geofenceRadiusMeters = validateGeofenceRadius(geofenceRadiusMeters);
        this.active = active;
    }

    public static Branch create(
            TenantId tenantId,
            String name,
            String sunatCode,
            GeoPoint location,
            int geofenceRadiusMeters) {
        return new Branch(
                BranchId.generate(),
                tenantId,
                name,
                sunatCode,
                location,
                geofenceRadiusMeters,
                true
        );
    }

    /**
     * Determines whether the given GPS coordinates fall within this branch's geofenced radius
     * using the spherical Haversine distance formula.
     *
     * @param coordinates client or vehicle GPS coordinates to evaluate
     * @return true if the distance is less than or equal to the geofence radius
     */
    public boolean isWithinGeofence(GeoPoint coordinates) {
        Objects.requireNonNull(coordinates, "Coordinates to evaluate cannot be null");
        return this.location.distanceTo(coordinates).isWithinThreshold(this.geofenceRadiusMeters);
    }

    public void updateDetails(String name, String sunatCode, GeoPoint location, int geofenceRadiusMeters) {
        this.name = validateName(name);
        this.sunatCode = validateSunatCode(sunatCode);
        this.location = Objects.requireNonNull(location, "GPS location cannot be null");
        this.geofenceRadiusMeters = validateGeofenceRadius(geofenceRadiusMeters);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public BranchId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public String name() {
        return name;
    }

    public String sunatCode() {
        return sunatCode;
    }

    public GeoPoint location() {
        return location;
    }

    public int geofenceRadiusMeters() {
        return geofenceRadiusMeters;
    }

    public boolean isActive() {
        return active;
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "Branch name cannot be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be empty");
        }
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Branch name cannot exceed 100 characters");
        }
        return trimmed;
    }

    private static String validateSunatCode(String code) {
        Objects.requireNonNull(code, "SUNAT establishment code cannot be null");
        String trimmed = code.trim();
        if (!trimmed.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("SUNAT establishment code must be exactly 4 digits");
        }
        return trimmed;
    }

    private static int validateGeofenceRadius(int radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Geofence radius must be strictly positive");
        }
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Branch branch)) return false;
        return Objects.equals(id, branch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
