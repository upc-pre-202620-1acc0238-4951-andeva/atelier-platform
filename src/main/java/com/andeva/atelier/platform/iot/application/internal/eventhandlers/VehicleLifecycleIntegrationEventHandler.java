package com.andeva.atelier.platform.iot.application.internal.eventhandlers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleDecommissionedIntegrationEvent;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleOwnershipTransferredIntegrationEvent;
import com.andeva.atelier.platform.iot.interfaces.events.WorkOrderCompletedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Integration Event Handler listening to cross-bounded-context lifecycle events from CRM and MRO.
 * Safely unbinds hardware upon vehicle decommission or transfer, and auto-resolves DTC faults on work order completion.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VehicleLifecycleIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(VehicleLifecycleIntegrationEventHandler.class);

    private final DeviceInstallationRepository deviceInstallationRepository;
    private final VehicleFaultRepository vehicleFaultRepository;

    public VehicleLifecycleIntegrationEventHandler(
            DeviceInstallationRepository deviceInstallationRepository,
            VehicleFaultRepository vehicleFaultRepository
    ) {
        this.deviceInstallationRepository = Objects.requireNonNull(deviceInstallationRepository, "DeviceInstallationRepository cannot be null");
        this.vehicleFaultRepository = Objects.requireNonNull(vehicleFaultRepository, "VehicleFaultRepository cannot be null");
    }

    @EventListener
    @Transactional
    public void on(VehicleDecommissionedIntegrationEvent event) {
        log.info("Handling VehicleDecommissionedIntegrationEvent for vehicleId: {}", event.vehicleId());
        VehicleId vehicleId = VehicleId.of(event.vehicleId());

        Optional<DeviceInstallation> activeInstallation = deviceInstallationRepository.findActiveByVehicleId(vehicleId);
        activeInstallation.ifPresent(installation -> {
            installation.uninstall(installation.getInitialOdometerKm(), event.occurredOn());
            deviceInstallationRepository.save(installation);
            log.info("Auto-uninstalled device {} from decommissioned vehicle {}",
                    installation.getDeviceId(), vehicleId);
        });
    }

    @EventListener
    @Transactional
    public void on(VehicleOwnershipTransferredIntegrationEvent event) {
        log.info("Handling VehicleOwnershipTransferredIntegrationEvent for vehicleId: {}", event.vehicleId());
        VehicleId vehicleId = VehicleId.of(event.vehicleId());

        Optional<DeviceInstallation> activeInstallation = deviceInstallationRepository.findActiveByVehicleId(vehicleId);
        activeInstallation.ifPresent(installation -> {
            installation.uninstall(installation.getInitialOdometerKm(), event.occurredOn());
            deviceInstallationRepository.save(installation);
            log.info("Auto-uninstalled device {} due to ownership transfer on vehicle {}",
                    installation.getDeviceId(), vehicleId);
        });
    }

    @EventListener
    @Transactional
    public void on(WorkOrderCompletedIntegrationEvent event) {
        log.info("Handling WorkOrderCompletedIntegrationEvent for vehicleId: {}, workOrderId: {}",
                event.vehicleId(), event.workOrderId());
        VehicleId vehicleId = VehicleId.of(event.vehicleId());

        List<VehicleFault> activeFaults = vehicleFaultRepository.findActiveByVehicleId(vehicleId);
        for (VehicleFault fault : activeFaults) {
            if (event.resolvedDtcCodes().contains(fault.getDtcCode().value())) {
                fault.markResolved(event.occurredOn() != null ? event.occurredOn() : Instant.now());
                vehicleFaultRepository.save(fault);
                log.info("Auto-resolved DTC fault {} on vehicle {} following completed work order",
                        fault.getDtcCode().value(), vehicleId);
            }
        }
    }
}
