package com.andeva.atelier.platform.inventory.application.internal.eventhandlers;

import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.domain.model.commands.AllocateStockFifoCommand;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.inventory.interfaces.events.StockAllocatedIntegrationEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.StockReservationFailedIntegrationEvent;
import com.andeva.atelier.platform.operations.interfaces.events.ProductStockReservationRequestedIntegrationEvent;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Component
public class WorkOrderStockReservationRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderStockReservationRequestedListener.class);

    private final InventoryItemCommandService itemCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public WorkOrderStockReservationRequestedListener(
            InventoryItemCommandService itemCommandService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.itemCommandService = Objects.requireNonNull(itemCommandService, "itemCommandService cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @EventListener
    @Transactional
    public void on(ProductStockReservationRequestedIntegrationEvent event) {
        log.info("Processing stock reservation request for product {} in work order {} task {}",
                event.productId(), event.workOrderId(), event.taskId());

        AllocateStockFifoCommand command = new AllocateStockFifoCommand(
                InventoryItemId.of(event.productId()),
                Quantity.of(event.quantity()),
                event.workOrderId(),
                event.taskId()
        );

        Result<StockAllocation, ApplicationError> result = itemCommandService.handle(command);
        if (result.isSuccess()) {
            StockAllocation allocation = result.getOrThrow();
            StockAllocatedIntegrationEvent allocatedEvent = new StockAllocatedIntegrationEvent(
                    allocation.allocationId(),
                    event.productId(),
                    allocation.allocatedQuantity().value(),
                    allocation.totalCogs().amount(),
                    Instant.now()
            );
            eventPublisher.publishEvent(allocatedEvent);
        } else {
            log.error("Failed to allocate stock for product {}: {}", event.productId(), result.getError().message());
            StockReservationFailedIntegrationEvent failedEvent = new StockReservationFailedIntegrationEvent(
                    event.productId(),
                    event.workOrderId(),
                    event.taskId(),
                    event.quantity(),
                    result.getError().message(),
                    Instant.now()
            );
            eventPublisher.publishEvent(failedEvent);
        }
    }
}
