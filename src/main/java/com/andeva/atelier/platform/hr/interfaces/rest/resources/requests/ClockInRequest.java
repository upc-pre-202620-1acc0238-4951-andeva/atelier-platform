package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * @param clockInTime optional moment captured on the device, for a clock-in queued while offline
 *                    (at most 12 hours old and not in the future)
 */
public record ClockInRequest(
        @NotNull UUID branchId,
        @NotNull UUID shiftId,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        Instant clockInTime
) {
    public ClockInRequest(UUID branchId, UUID shiftId, Double latitude, Double longitude) {
        this(branchId, shiftId, latitude, longitude, null);
    }
}
