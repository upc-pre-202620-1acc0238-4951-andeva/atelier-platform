package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when an active OBD-II Diagnostic Trouble Code (DTC) is flagged by a vehicle ECU.
 *
 * @author Joel Huamani Estefanero
 */
public record VehicleFaultDetectedEvent(
        FaultId faultId,
        VehicleId vehicleId,
        TenantId tenantId,
        DtcCode dtcCode,
        FaultSeverity severity,
        Instant occurredOn
) implements Serializable {

    public VehicleFaultDetectedEvent {
        Objects.requireNonNull(faultId, "FaultId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(dtcCode, "DtcCode cannot be null");
        Objects.requireNonNull(severity, "FaultSeverity cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static VehicleFaultDetectedEvent of(
            FaultId faultId,
            VehicleId vehicleId,
            TenantId tenantId,
            DtcCode dtcCode,
            FaultSeverity severity
    ) {
        return new VehicleFaultDetectedEvent(faultId, vehicleId, tenantId, dtcCode, severity, Instant.now());
    }
}
