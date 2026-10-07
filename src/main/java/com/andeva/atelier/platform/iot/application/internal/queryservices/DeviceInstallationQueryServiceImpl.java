package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.queryservices.DeviceInstallationQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByVehicleIdQuery;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Query Service implementation for vehicle hardware installation bindings and historical telemetry links.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class DeviceInstallationQueryServiceImpl implements DeviceInstallationQueryService {

    private final DeviceInstallationRepository deviceInstallationRepository;

    public DeviceInstallationQueryServiceImpl(DeviceInstallationRepository deviceInstallationRepository) {
        this.deviceInstallationRepository = Objects.requireNonNull(deviceInstallationRepository, "DeviceInstallationRepository cannot be null");
    }

    @Override
    public Optional<DeviceInstallation> handle(GetDeviceByVehicleIdQuery query) {
        Objects.requireNonNull(query, "GetDeviceByVehicleIdQuery cannot be null");
        return deviceInstallationRepository.findActiveByVehicleId(query.vehicleId());
    }

    @Override
    public Optional<DeviceInstallation> findById(com.andeva.atelier.platform.iot.domain.model.ids.InstallationId installationId) {
        Objects.requireNonNull(installationId, "InstallationId cannot be null");
        return deviceInstallationRepository.findById(installationId);
    }

    @Override
    public List<DeviceInstallation> getInstallationHistoryByVehicle(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return deviceInstallationRepository.findAllHistoryByVehicleId(vehicleId);
    }
}
