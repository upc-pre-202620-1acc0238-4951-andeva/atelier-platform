package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain command to record a newly detected diagnostic trouble code (DTC) from a vehicle ECU.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterVehicleFaultCommand(
        VehicleId vehicleId,
        TenantId tenantId,
        DtcCode code,
        FaultSeverity severity,
        String description
) implements Serializable {

    public RegisterVehicleFaultCommand {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(code, "DtcCode cannot be null");
        Objects.requireNonNull(severity, "FaultSeverity cannot be null");
        description = description != null ? description.trim() : "";
    }
}
