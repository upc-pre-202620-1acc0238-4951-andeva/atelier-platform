package com.andeva.atelier.platform.iot.application.queryservices;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;

/**
 * Query Service for reading active and historical vehicle DTC faults.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleFaultQueryService {

    /**
     * Retrieves all currently active and unresolved DTC faults for the specified vehicle.
     *
     * @param vehicleId target vehicle identifier
     * @return list of active VehicleFault aggregates
     */
    List<VehicleFault> getActiveFaultsByVehicle(VehicleId vehicleId);

    /**
     * Retrieves chronological fault history for the specified vehicle including resolved faults.
     *
     * @param vehicleId target vehicle identifier
     * @return list of historical VehicleFault aggregates
     */
    /**
     * Finds a single vehicle fault by its strongly-typed identifier.
     *
     * @param faultId unique fault identifier
     * @return optional VehicleFault aggregate
     */
    java.util.Optional<VehicleFault> findById(com.andeva.atelier.platform.iot.domain.model.ids.FaultId faultId);

    List<VehicleFault> getFaultHistoryByVehicle(VehicleId vehicleId);
}
