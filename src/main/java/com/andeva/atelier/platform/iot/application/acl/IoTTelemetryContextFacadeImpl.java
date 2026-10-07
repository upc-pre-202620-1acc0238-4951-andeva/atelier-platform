package com.andeva.atelier.platform.iot.application.acl;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.TelemetryLogRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.interfaces.acl.IoTTelemetryContextFacade;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.ActiveVehicleFaultsDto;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.VehicleLatestTelemetryDto;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.VehicleTelemetryHealthDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of the Open Host Service facade {@link IoTTelemetryContextFacade}.
 * Serves real-time telemetry, active DTC alerts, and health scores to downstream contexts.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class IoTTelemetryContextFacadeImpl implements IoTTelemetryContextFacade {

    private final TelemetryLogRepository telemetryLogRepository;
    private final VehicleFaultRepository vehicleFaultRepository;
    private final DeviceInstallationRepository deviceInstallationRepository;

    public IoTTelemetryContextFacadeImpl(
            TelemetryLogRepository telemetryLogRepository,
            VehicleFaultRepository vehicleFaultRepository,
            DeviceInstallationRepository deviceInstallationRepository
    ) {
        this.telemetryLogRepository = Objects.requireNonNull(telemetryLogRepository, "TelemetryLogRepository cannot be null");
        this.vehicleFaultRepository = Objects.requireNonNull(vehicleFaultRepository, "VehicleFaultRepository cannot be null");
        this.deviceInstallationRepository = Objects.requireNonNull(deviceInstallationRepository, "DeviceInstallationRepository cannot be null");
    }

    @Override
    public Optional<VehicleLatestTelemetryDto> getVehicleLatestTelemetry(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return telemetryLogRepository.findLatestByVehicleId(vehicleId)
                .map(r -> new VehicleLatestTelemetryDto(
                        r.vehicleId().value(),
                        r.timestamp(),
                        r.speed().kmh(),
                        r.engineTemperature().celsius(),
                        r.engineRpm().rpm(),
                        r.batteryVoltage().map(b -> b.volts()).orElse(null)
                ));
    }

    @Override
    public List<ActiveVehicleFaultsDto> getActiveFaultsForVehicle(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return vehicleFaultRepository.findActiveByVehicleId(vehicleId).stream()
                .map(f -> new ActiveVehicleFaultsDto(
                        f.getId().value(),
                        f.getDtcCode().value(),
                        f.getSeverity().name(),
                        f.getDescription(),
                        f.getDetectedAt()
                ))
                .toList();
    }

    @Override
    public boolean hasActiveDeviceInstallation(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return deviceInstallationRepository.findActiveByVehicleId(vehicleId).isPresent();
    }

    @Override
    public VehicleTelemetryHealthDto getVehicleTelemetryHealth(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        List<VehicleFault> activeFaults = vehicleFaultRepository.findActiveByVehicleId(vehicleId);
        int faultCount = activeFaults.size();
        int score = Math.max(0, 100 - (faultCount * 20));
        String trafficLight = score >= 80 ? "GREEN" : score >= 50 ? "YELLOW" : "RED";
        return new VehicleTelemetryHealthDto(
                vehicleId.value(),
                score,
                trafficLight,
                faultCount,
                faultCount > 0
        );
    }
}
