package com.andeva.atelier.platform.iot.application.internal.commandservices;

import com.andeva.atelier.platform.iot.application.commandservices.Obd2DeviceCommandService;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.IoTSubscriptionQuotaPort;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDeviceIdentifierException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterObd2DeviceCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UpdateDeviceStatusCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.repositories.Obd2DeviceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Command Service implementation for registering OBD-II hardware scanners
 * and managing their lifecycle status.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class Obd2DeviceCommandServiceImpl implements Obd2DeviceCommandService {

    private final Obd2DeviceRepository obd2DeviceRepository;
    private final IoTSubscriptionQuotaPort subscriptionQuotaPort;
    private final ApplicationEventPublisher eventPublisher;

    public Obd2DeviceCommandServiceImpl(
            Obd2DeviceRepository obd2DeviceRepository,
            IoTSubscriptionQuotaPort subscriptionQuotaPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.obd2DeviceRepository = Objects.requireNonNull(obd2DeviceRepository, "Obd2DeviceRepository cannot be null");
        this.subscriptionQuotaPort = Objects.requireNonNull(subscriptionQuotaPort, "IoTSubscriptionQuotaPort cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public DeviceId handle(RegisterObd2DeviceCommand command) {
        Objects.requireNonNull(command, "RegisterObd2DeviceCommand cannot be null");

        // 1. Enforce SaaS subscription plan quota for connected active scanners
        int activeDevices = (int) obd2DeviceRepository.findAllByTenantId(command.tenantId())
                .stream()
                .filter(d -> d.getStatus() == DeviceStatus.ACTIVE)
                .count();

        subscriptionQuotaPort.validateObd2DeviceRegistrationAllowed(command.tenantId(), activeDevices);

        // 2. Enforce physical identifier uniqueness (MAC / IMEI)
        if (obd2DeviceRepository.existsByIdentifier(command.identifier())) {
            throw new InvalidDeviceIdentifierException(
                    command.identifier().value(),
                    "Device with hardware identifier already registered in inventory"
            );
        }

        // 3. Register device and publish lifecycle events
        Obd2Device device = Obd2Device.register(
                command.tenantId(),
                command.identifier(),
                command.type(),
                command.model(),
                command.firmware()
        );

        obd2DeviceRepository.save(device);
        device.domainEvents().forEach(eventPublisher::publishEvent);

        return device.getId();
    }

    @Override
    public void handle(UpdateDeviceStatusCommand command) {
        Objects.requireNonNull(command, "UpdateDeviceStatusCommand cannot be null");

        Obd2Device device = obd2DeviceRepository.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId()));

        switch (command.newStatus()) {
            case ACTIVE -> device.markActive();
            case INACTIVE -> device.markInactive();
            case LOST -> device.markLost();
            case BROKEN -> device.markBroken();
        }

        obd2DeviceRepository.save(device);
        device.domainEvents().forEach(eventPublisher::publishEvent);
    }
}
