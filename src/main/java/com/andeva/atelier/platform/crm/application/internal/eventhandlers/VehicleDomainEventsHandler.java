package com.andeva.atelier.platform.crm.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.domain.model.events.VehicleOwnershipTransferredEvent;
import com.andeva.atelier.platform.crm.domain.model.events.VehicleRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Application event listener orchestrating side effects on vehicle domain events.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class VehicleDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(VehicleDomainEventsHandler.class);

    @EventListener
    @Async
    public void on(VehicleRegisteredEvent event) {
        log.info("Processing VehicleRegisteredEvent for vehicle ID: {}, plate: {}",
                event.vehicleId(), event.plate().value());
    }

    @EventListener
    @Async
    public void on(VehicleOwnershipTransferredEvent event) {
        log.info("Processing VehicleOwnershipTransferredEvent for vehicle ID: {}, from: {} to: {}",
                event.vehicleId(), event.previousOwnerId(), event.newOwnerId());
    }
}
