package com.andeva.atelier.platform.iot.application.internal.eventhandlers;

import com.andeva.atelier.platform.iot.domain.model.events.VehicleFaultDetectedEvent;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleFaultLoggedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Domain Event Handler reacting to newly detected vehicle DTC faults.
 * Emits {@link VehicleFaultLoggedIntegrationEvent} to enrich vehicular service history.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VehicleFaultDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(VehicleFaultDomainEventHandler.class);

    private final VehicleFaultRepository vehicleFaultRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VehicleFaultDomainEventHandler(
            VehicleFaultRepository vehicleFaultRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.vehicleFaultRepository = Objects.requireNonNull(vehicleFaultRepository, "VehicleFaultRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @EventListener
    public void on(VehicleFaultDetectedEvent event) {
        log.info("Processing VehicleFaultDetectedEvent for faultId: {}", event.faultId());

        vehicleFaultRepository.findById(event.faultId()).ifPresent(fault -> {
            VehicleFaultLoggedIntegrationEvent integrationEvent = new VehicleFaultLoggedIntegrationEvent(
                    fault.getId().value(),
                    fault.getTenantId().value(),
                    fault.getVehicleId().value(),
                    fault.getDtcCode().value(),
                    fault.getSeverity().name(),
                    fault.getDescription(),
                    fault.getDetectedAt()
            );

            eventPublisher.publishEvent(integrationEvent);
            log.debug("Published VehicleFaultLoggedIntegrationEvent for fault: {}", fault.getId());
        });
    }
}
