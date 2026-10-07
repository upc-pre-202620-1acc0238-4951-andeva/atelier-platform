package com.andeva.atelier.platform.iot.application.internal.commandservices;

import com.andeva.atelier.platform.iot.application.commandservices.DeviceInstallationCommandService;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.domain.exceptions.ActiveInstallationConflictException;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceAlreadyInstalledException;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.InstallationNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.commands.InstallDeviceOnVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UninstallDeviceFromVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.Obd2DeviceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * Command Service implementation for physical installation and uninstallation of OBD-II devices.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class DeviceInstallationCommandServiceImpl implements DeviceInstallationCommandService {

    private final DeviceInstallationRepository deviceInstallationRepository;
    private final Obd2DeviceRepository obd2DeviceRepository;
    private final CrmFleetAclPort crmFleetAclPort;
    private final ApplicationEventPublisher eventPublisher;

    public DeviceInstallationCommandServiceImpl(
            DeviceInstallationRepository deviceInstallationRepository,
            Obd2DeviceRepository obd2DeviceRepository,
            CrmFleetAclPort crmFleetAclPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.deviceInstallationRepository = Objects.requireNonNull(deviceInstallationRepository, "DeviceInstallationRepository cannot be null");
        this.obd2DeviceRepository = Objects.requireNonNull(obd2DeviceRepository, "Obd2DeviceRepository cannot be null");
        this.crmFleetAclPort = Objects.requireNonNull(crmFleetAclPort, "CrmFleetAclPort cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public InstallationId handle(InstallDeviceOnVehicleCommand command) {
        Objects.requireNonNull(command, "InstallDeviceOnVehicleCommand cannot be null");

        // 1. Validate device exists and is in operational ACTIVE status
        Obd2Device device = obd2DeviceRepository.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId()));

        if (device.getStatus() != DeviceStatus.ACTIVE) {
            throw new IllegalStateException(
                    String.format("Device %s is in status %s and cannot be installed.", command.deviceId(), device.getStatus())
            );
        }

        if (!device.getTenantId().equals(command.tenantId())) {
            throw new IllegalStateException(
                    String.format("Device %s does not belong to tenant %s", command.deviceId(), command.tenantId())
            );
        }

        if (!crmFleetAclPort.isVehicleRegistered(command.vehicleId())) {
            throw new IllegalArgumentException(
                    String.format("Vehicle %s is not registered in CRM", command.vehicleId())
            );
        }

        // 2. Validate device is not currently mounted on another vehicle
        if (deviceInstallationRepository.findActiveByDeviceId(command.deviceId()).isPresent()) {
            throw new DeviceAlreadyInstalledException(command.deviceId());
        }

        // 3. Validate vehicle does not already have an active installation
        if (deviceInstallationRepository.findActiveByVehicleId(command.vehicleId()).isPresent()) {
            throw new ActiveInstallationConflictException(command.vehicleId());
        }

        // 4. Create and persist installation session
        DeviceInstallation installation = DeviceInstallation.install(
                command.deviceId(),
                command.vehicleId(),
                command.tenantId(),
                command.currentOdometerKm()
        );

        deviceInstallationRepository.save(installation);
        installation.domainEvents().forEach(eventPublisher::publishEvent);

        return installation.getId();
    }

    @Override
    public void handle(UninstallDeviceFromVehicleCommand command) {
        Objects.requireNonNull(command, "UninstallDeviceFromVehicleCommand cannot be null");

        DeviceInstallation installation = deviceInstallationRepository.findById(command.installationId())
                .orElseThrow(() -> new InstallationNotFoundException(command.installationId()));

        installation.uninstall(command.finalOdometerKm(), Instant.now());
        deviceInstallationRepository.save(installation);
        installation.domainEvents().forEach(eventPublisher::publishEvent);
    }
}
