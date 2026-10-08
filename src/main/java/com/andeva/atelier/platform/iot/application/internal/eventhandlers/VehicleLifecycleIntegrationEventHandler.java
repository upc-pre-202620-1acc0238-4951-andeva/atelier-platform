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
import java.util.UUID;

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
        handleVehicleHardwareUnbind(VehicleId.of(event.vehicleId()), event.occurredOn(), "vehicle decommission");
    }

    @EventListener
    @Transactional
    public void on(VehicleOwnershipTransferredIntegrationEvent event) {
        log.info("Handling VehicleOwnershipTransferredIntegrationEvent for vehicleId: {}", event.vehicleId());
        handleVehicleHardwareUnbind(VehicleId.of(event.vehicleId()), event.occurredOn(), "ownership transfer");
    }

    @EventListener
    @Transactional
    public void on(com.andeva.atelier.platform.crm.interfaces.events.VehicleOwnershipTransferredIntegrationEvent event) {
        log.info("Handling CRM VehicleOwnershipTransferredIntegrationEvent for vehicleId: {}", event.vehicleId());
        handleVehicleHardwareUnbind(VehicleId.of(event.vehicleId()), event.occurredOn(), "CRM ownership transfer");
    }

    @EventListener
    @Transactional
    public void on(WorkOrderCompletedIntegrationEvent event) {
        log.info("Handling WorkOrderCompletedIntegrationEvent for vehicleId: {}, workOrderId: {}",
                event.vehicleId(), event.workOrderId());
        handleWorkOrderFaultResolution(VehicleId.of(event.vehicleId()), event.workOrderId(), event.resolvedDtcCodes(), event.occurredOn());
    }

    @EventListener
    @Transactional
    public void on(com.andeva.atelier.platform.operations.interfaces.events.WorkOrderCompletedIntegrationEvent event) {
        log.info("Handling Operations WorkOrderCompletedIntegrationEvent for vehicleId: {}, workOrderId: {}",
                event.vehicleId(), event.workOrderId());
        handleWorkOrderFaultResolution(VehicleId.of(event.vehicleId()), event.workOrderId(), null, event.occurredOn());
    }

    private void handleVehicleHardwareUnbind(VehicleId vehicleId, Instant occurredOn, String triggerReason) {
        Optional<DeviceInstallation> activeInstallation = deviceInstallationRepository.findActiveByVehicleId(vehicleId);
        activeInstallation.ifPresent(installation -> {
            installation.uninstall(installation.getInitialOdometerKm(), occurredOn != null ? occurredOn : Instant.now());
            deviceInstallationRepository.save(installation);
            log.info("Auto-uninstalled device {} due to {} on vehicle {}",
                    installation.getDeviceId(), triggerReason, vehicleId);
        });
    }

    private void handleWorkOrderFaultResolution(VehicleId vehicleId, UUID workOrderId, List<String> resolvedDtcCodes, Instant occurredOn) {
        List<VehicleFault> activeFaults = vehicleFaultRepository.findActiveByVehicleId(vehicleId);
        Instant resolvedAt = occurredOn != null ? occurredOn : Instant.now();
        for (VehicleFault fault : activeFaults) {
            boolean shouldResolve = (resolvedDtcCodes == null || resolvedDtcCodes.isEmpty())
                    || resolvedDtcCodes.contains(fault.getDtcCode().value());
            if (shouldResolve) {
                fault.markResolved(resolvedAt);
                vehicleFaultRepository.save(fault);
                log.info("Auto-resolved DTC fault {} on vehicle {} following completed work order {}",
                        fault.getDtcCode().value(), vehicleId, workOrderId);
            }
        }
    }
}
