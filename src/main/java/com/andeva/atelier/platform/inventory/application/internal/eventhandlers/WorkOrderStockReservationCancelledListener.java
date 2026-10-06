package com.andeva.atelier.platform.inventory.application.internal.eventhandlers;

import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReleaseStockAllocationCommand;
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

    @EventListener
    public void on(ProductStockReservationCancelledIntegrationEvent event) {
        log.info("Processing stock reservation cancellation for product {} in work order {} task {}",
                event.productId(), event.workOrderId(), event.taskId());

        ReleaseStockAllocationCommand command = ReleaseStockAllocationCommand.ofBatch(
                InventoryItemId.of(event.productId()),
                null,
                Quantity.of(event.quantity()),
                event.workOrderId(),
                "Cancelled MRO task product reservation"
        );

        Result<Void, ApplicationError> result = itemCommandService.handle(command);
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
