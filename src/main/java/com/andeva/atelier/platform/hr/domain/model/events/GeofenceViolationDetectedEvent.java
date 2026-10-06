package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record GeofenceViolationDetectedEvent(
        TenantId tenantId,
        BranchId branchId,
        TenantMembershipId membershipId,
        GeoCoordinates attemptedCoordinates,
        double calculatedDistanceMeters,
        double allowedRadiusMeters,
        Instant occurredOn
) implements Serializable {

    public GeofenceViolationDetectedEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(attemptedCoordinates, "attemptedCoordinates cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static GeofenceViolationDetectedEvent now(
            TenantId tenantId,
            BranchId branchId,
            TenantMembershipId membershipId,
            GeoCoordinates attemptedCoordinates,
            double calculatedDistanceMeters,
            double allowedRadiusMeters
    ) {
        return new GeofenceViolationDetectedEvent(
                tenantId, branchId, membershipId, attemptedCoordinates, calculatedDistanceMeters, allowedRadiusMeters, Instant.now()
        );
    }
}
