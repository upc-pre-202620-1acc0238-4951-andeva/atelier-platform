package com.andeva.atelier.platform.iot.domain.repositories;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound domain repository port for managing VehicleFault aggregate roots.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleFaultRepository {

    VehicleFault save(VehicleFault fault);

    Optional<VehicleFault> findById(FaultId id);

    List<VehicleFault> findActiveByVehicleId(VehicleId vehicleId);

    List<VehicleFault> findAllByVehicleId(VehicleId vehicleId);

    List<VehicleFault> findAllByTenantId(TenantId tenantId);
}
