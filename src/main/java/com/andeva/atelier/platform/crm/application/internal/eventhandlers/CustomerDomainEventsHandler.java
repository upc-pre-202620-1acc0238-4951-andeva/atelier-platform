package com.andeva.atelier.platform.crm.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.domain.model.events.CustomerContactUpdatedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.CustomerRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Application event listener orchestrating side effects on customer domain events.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class CustomerDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerDomainEventsHandler.class);

    @EventListener
    @Async
    public void on(CustomerRegisteredEvent event) {
        log.info("Processing CustomerRegisteredEvent for customer ID: {}, tenant: {}, type: {}",
                event.customerId(), event.tenantId(), event.type());
    }

    @EventListener
    @Async
    public void on(CustomerContactUpdatedEvent event) {
        log.info("Processing CustomerContactUpdatedEvent for customer ID: {}", event.customerId());
    }
}
