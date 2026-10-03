package com.andeva.atelier.platform.crm.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.DriverAppPushGateway;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentArrivedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentCanceledEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentConfirmedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.AppointmentScheduledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Application event listener orchestrating side effects on appointment domain events.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class AppointmentDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentDomainEventsHandler.class);

    private final DriverAppPushGateway driverAppPushGateway;

    public AppointmentDomainEventsHandler(DriverAppPushGateway driverAppPushGateway) {
        this.driverAppPushGateway = Objects.requireNonNull(driverAppPushGateway, "DriverAppPushGateway cannot be null");
    }

    @EventListener
    @Async
    public void on(AppointmentScheduledEvent event) {
        log.info("Processing AppointmentScheduledEvent for appointment ID: {}, scheduledAt: {}",
                event.appointmentId(), event.scheduledAt());
    }

    @EventListener
    @Async
    public void on(AppointmentConfirmedEvent event) {
        log.info("Processing AppointmentConfirmedEvent for appointment ID: {}", event.appointmentId());
        driverAppPushGateway.sendPushNotification(
                "token_placeholder",
                "Appointment Confirmed",
                "Your appointment has been confirmed for " + event.scheduledAt(),
                Map.of("appointmentId", event.appointmentId().value().toString())
        );
    }

    @EventListener
    @Async
    public void on(AppointmentArrivedEvent event) {
        log.info("Processing AppointmentArrivedEvent for appointment ID: {}, vehicle arrived at workshop",
                event.appointmentId());
    }

    @EventListener
    @Async
    public void on(AppointmentCanceledEvent event) {
        log.info("Processing AppointmentCanceledEvent for appointment ID: {}, reason: {}",
                event.appointmentId(), event.cancellationReason());
    }
}
