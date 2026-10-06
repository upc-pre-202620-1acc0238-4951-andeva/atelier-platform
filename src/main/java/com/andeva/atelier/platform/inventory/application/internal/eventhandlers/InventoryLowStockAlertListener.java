package com.andeva.atelier.platform.inventory.application.internal.eventhandlers;

import com.andeva.atelier.platform.inventory.domain.model.events.LowStockThresholdReachedEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.LowStockAlertIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class InventoryLowStockAlertListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryLowStockAlertListener.class);

    private final ApplicationEventPublisher eventPublisher;

    public InventoryLowStockAlertListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @EventListener
    public void on(LowStockThresholdReachedEvent event) {
        log.warn("CRITICAL: Inventory item {} reached low stock threshold. Current stock: {}, Minimum stock: {}",
                event.itemId().value(), event.currentStock().value(), event.minimumStock().value());

        LowStockAlertIntegrationEvent integrationEvent = new LowStockAlertIntegrationEvent(
                event.itemId().value(),
                event.tenantId().value(),
                event.currentStock().value(),
                event.minimumStock().value(),
                Instant.now()
        );
        eventPublisher.publishEvent(integrationEvent);
    }
}
