package com.andeva.atelier.platform.iot.interfaces.acl;

import com.andeva.atelier.platform.iot.interfaces.acl.dto.ActiveVehicleFaultsDto;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.VehicleLatestTelemetryDto;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.VehicleTelemetryHealthDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.Optional;

/**
 * Inbound Anti-Corruption Layer (ACL) and Open Host Service (OHS) exposing real-time telemetry,
 * active DTC faults, and overall vehicle health to downstream bounded contexts.
 *
 * @author Joel Huamani Estefanero
 */
public interface IoTTelemetryContextFacade {

    /**
     * Retrieves the most recent telemetry readings recorded for the specified vehicle.
     *
     * @param vehicleId target vehicle identifier
     * @return optional VehicleLatestTelemetryDto
     */
    Optional<VehicleLatestTelemetryDto> getVehicleLatestTelemetry(VehicleId vehicleId);

    /**
     * Retrieves the active, unresolved diagnostic trouble codes (DTCs) logged for the vehicle.
     *
     * @param vehicleId target vehicle identifier
     * @return list of active DTC fault DTOs
     */
    List<ActiveVehicleFaultsDto> getActiveFaultsForVehicle(VehicleId vehicleId);

    /**
     * Checks whether the vehicle is actively equipped with an operational OBD-II device.
     *
     * @param vehicleId target vehicle identifier
     * @return true if an active installation exists
     */
    boolean hasActiveDeviceInstallation(VehicleId vehicleId);

    /**
     * Computes an overall mechanical and electrical health score (0-100) and traffic light for the vehicle.
     *
     * @param vehicleId target vehicle identifier
     * @return VehicleTelemetryHealthDto
     */
    VehicleTelemetryHealthDto getVehicleTelemetryHealth(VehicleId vehicleId);
}
