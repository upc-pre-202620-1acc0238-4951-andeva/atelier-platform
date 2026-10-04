package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.Objects;

/**
 * Command for updating vehicle technical specifications.
 *
 * @author Adiel Sanchez Santin
 */
public record UpdateVehicleCommand(
        VehicleId vehicleId,
        String brand,
        String model,
        int year,
        EngineType engineType
) {
    public UpdateVehicleCommand {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(brand, "Brand cannot be null");
        Objects.requireNonNull(model, "Model cannot be null");
        Objects.requireNonNull(engineType, "EngineType cannot be null");
    }
}
