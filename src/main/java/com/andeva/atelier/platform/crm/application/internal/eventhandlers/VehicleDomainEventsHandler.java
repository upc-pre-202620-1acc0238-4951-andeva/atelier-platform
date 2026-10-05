package com.andeva.atelier.platform.crm.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.events.VehicleOwnershipTransferredEvent;
import com.andeva.atelier.platform.crm.domain.model.events.VehicleRegisteredEvent;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.interfaces.events.VehicleOwnershipTransferredIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.VehicleRegisteredIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Application event listener orchestrating side effects on vehicle domain events.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class VehicleDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(VehicleDomainEventsHandler.class);

    private final VehicleRepository vehicleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VehicleDomainEventsHandler(
            VehicleRepository vehicleRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @EventListener
    public void on(VehicleRegisteredEvent event) {
        log.info("Processing VehicleRegisteredEvent for vehicle ID: {}, plate: {}",
                event.vehicleId(), event.plate().value());

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(event.vehicleId());
        String brand = vehicleOpt.map(Vehicle::brand).orElse("Unknown");
        String model = vehicleOpt.map(Vehicle::model).orElse("Unknown");
        int year = vehicleOpt.map(Vehicle::year).orElse(2024);
        String engineType = vehicleOpt.map(v -> v.engineType().name()).orElse("GASOLINE");
        String vin = vehicleOpt.map(v -> v.vin() != null ? v.vin().value() : null).orElse(null);

        VehicleRegisteredIntegrationEvent integrationEvent = new VehicleRegisteredIntegrationEvent(
                event.vehicleId().value(),
                event.plate().value(),
                vin,
                brand,
                model,
                year,
                engineType,
                event.initialOwnerId() != null ? event.initialOwnerId().value() : null,
                event.occurredOn()
        );
        eventPublisher.publishEvent(integrationEvent);
    }

    @EventListener
    public void on(VehicleOwnershipTransferredEvent event) {
        log.info("Processing VehicleOwnershipTransferredEvent for vehicle ID: {}, from: {} to: {}",
                event.vehicleId(), event.previousOwnerId(), event.newOwnerId());

        VehicleOwnershipTransferredIntegrationEvent integrationEvent = new VehicleOwnershipTransferredIntegrationEvent(
                event.vehicleId().value(),
                event.previousOwnerId() != null ? event.previousOwnerId().value() : null,
                event.newOwnerId().value(),
                event.transferDate(),
                event.occurredOn()
        );
        eventPublisher.publishEvent(integrationEvent);
    }
}
