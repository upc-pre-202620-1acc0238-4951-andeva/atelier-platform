package com.andeva.atelier.platform.inventory.application.internal.eventhandlers;

import com.andeva.atelier.platform.inventory.application.internal.outbound.acl.InventoryStaffNotificationPort;
import com.andeva.atelier.platform.inventory.domain.model.events.LowStockThresholdReachedEvent;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Objects;

/**
 * US34 E1: whoever watches the stock alerts is warned the moment an item drops to its minimum (best-effort push).
 */
@Component
public class LowStockNotificationsEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LowStockNotificationsEventHandler.class);

    static final String ALERTS_PERMISSION = "inventory:alerts:read";

    private final InventoryStaffNotificationPort notificationPort;
    private final InventoryItemRepository itemRepository;

    public LowStockNotificationsEventHandler(InventoryStaffNotificationPort notificationPort, InventoryItemRepository itemRepository) {
        this.notificationPort = Objects.requireNonNull(notificationPort, "notificationPort cannot be null");
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository cannot be null");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(LowStockThresholdReachedEvent event) {
        try {
            String name = itemRepository.findById(event.itemId()).map(item -> item.getName()).orElse("Un repuesto");
            String current = event.currentStock().value().stripTrailingZeros().toPlainString();
            String minimum = event.minimumStock().value().stripTrailingZeros().toPlainString();
            notificationPort.notifyPermissionHolders(event.tenantId().value(), ALERTS_PERMISSION,
                    "Stock mínimo alcanzado",
                    name + ": quedan " + current + " (mínimo " + minimum + "). Requiere reposición.",
                    Map.of("type", "LOW_STOCK", "itemId", event.itemId().value().toString(),
                            "currentStock", current, "minimumStock", minimum));
        } catch (RuntimeException e) {
            log.warn("Low stock notification could not be delivered: {}", e.getMessage());
        }
    }
}
