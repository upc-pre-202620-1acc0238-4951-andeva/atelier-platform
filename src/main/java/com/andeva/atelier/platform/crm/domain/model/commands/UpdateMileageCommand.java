package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.Objects;

/**
 * Command for registering a new mileage reading with non-regression validation.
 *
 * @author Adiel Sanchez Santin
 */
public record UpdateMileageCommand(
        VehicleId vehicleId,
        int mileage,
        Instant recordedAt
) {
    public UpdateMileageCommand {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(recordedAt, "RecordedAt cannot be null");
    }
}
