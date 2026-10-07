package com.andeva.atelier.platform.iot.domain.repositories;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound domain repository port for managing DeviceInstallation aggregate roots.
 *
 * @author Joel Huamani Estefanero
 */
public interface DeviceInstallationRepository {

    DeviceInstallation save(DeviceInstallation installation);

    Optional<DeviceInstallation> findById(InstallationId id);

    Optional<DeviceInstallation> findActiveByVehicleId(VehicleId vehicleId);

    Optional<DeviceInstallation> findActiveByDeviceId(DeviceId deviceId);

    List<DeviceInstallation> findAllHistoryByVehicleId(VehicleId vehicleId);

    List<DeviceInstallation> findAllActiveByTenantId(TenantId tenantId);
}
