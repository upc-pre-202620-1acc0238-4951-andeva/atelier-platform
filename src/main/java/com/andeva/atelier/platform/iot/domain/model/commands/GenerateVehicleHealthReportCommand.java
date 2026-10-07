package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain command orchestrating the predictive vehicle health diagnosis via Spring AI and sensory analysis.
 *
 * @author Joel Huamani Estefanero
 */
public record GenerateVehicleHealthReportCommand(
        TenantId tenantId,
        VehicleId vehicleId,
        int daysToAnalyze,
        boolean includeResolvedDtcHistory
) implements Serializable {

    public GenerateVehicleHealthReportCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        if (daysToAnalyze < 1) {
            throw new IllegalArgumentException("Days to analyze must be at least 1: " + daysToAnalyze);
        }
    }
}
