package com.andeva.atelier.platform.iot.application.queryservices;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByVehicleIdQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.Optional;

/**
 * Query Service for inspecting hardware installation bindings on vehicles.
 *
 * @author Joel Huamani Estefanero
 */
public interface DeviceInstallationQueryService {

    /**
     * Finds the currently active OBD-II hardware installation for a vehicle.
     *
     * @param query query containing target vehicle identifier
     * @return optional DeviceInstallation aggregate
     */
    Optional<DeviceInstallation> handle(GetDeviceByVehicleIdQuery query);

    /**
     * Retrieves the complete chronological installation history for a vehicle.
     *
     * @param vehicleId target vehicle identifier
     * @return list of DeviceInstallation aggregates
     */
    /**
     * Finds a device installation session by its unique identifier.
     *
     * @param installationId unique installation identifier
     * @return optional DeviceInstallation aggregate
     */
    Optional<DeviceInstallation> findById(com.andeva.atelier.platform.iot.domain.model.ids.InstallationId installationId);

    List<DeviceInstallation> getInstallationHistoryByVehicle(VehicleId vehicleId);
}
