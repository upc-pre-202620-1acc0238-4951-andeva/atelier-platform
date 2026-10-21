package com.andeva.atelier.platform.inventory.application.internal.eventhandlers;

import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReturnWorkOrderPartsCommand;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.interfaces.events.StockReleasedIntegrationEvent;
import com.andeva.atelier.platform.operations.interfaces.events.ProductStockReservationCancelledIntegrationEvent;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class WorkOrderStockReservationCancelledListener {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderStockReservationCancelledListener.class);

    private final InventoryItemCommandService itemCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public WorkOrderStockReservationCancelledListener(
            InventoryItemCommandService itemCommandService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.itemCommandService = Objects.requireNonNull(itemCommandService, "itemCommandService cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    // Delivered after the publisher's commit: a transaction of its own is required for these writes to persist
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ProductStockReservationCancelledIntegrationEvent event) {
        log.info("Processing stock reservation cancellation for product {} in work order {} task {}",
                event.productId(), event.workOrderId(), event.taskId());

        // US33: the units go back to the batches they were taken from, with who returned them
        ReturnWorkOrderPartsCommand command = new ReturnWorkOrderPartsCommand(
                InventoryItemId.of(event.productId()),
                event.workOrderId(),
                event.taskId(),
                Quantity.of(event.quantity()),
                event.removedBy(),
                "Part withdrawn from the work order task"
        );

        Result<java.math.BigDecimal, ApplicationError> result = itemCommandService.handle(command);
        if (result.isSuccess()) {
            StockReleasedIntegrationEvent releasedEvent = new StockReleasedIntegrationEvent(
                    event.productId(),
                    event.quantity(),
                    Instant.now()
            );
            eventPublisher.publishEvent(releasedEvent);
        } else {
            log.warn("Could not release stock reservation for product {}: {}", event.productId(), result.getError().message());
        }
    }
}
