package com.andeva.atelier.platform.operations.application.internal.eventhandlers;

import com.andeva.atelier.platform.operations.domain.model.events.*;
import com.andeva.atelier.platform.operations.interfaces.events.ProductStockReservationCancelledIntegrationEvent;
import com.andeva.atelier.platform.operations.interfaces.events.ProductStockReservationRequestedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
public class WorkOrderTaskDomainEventsHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkOrderTaskDomainEventsHandler(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    }

    @EventListener
    public void on(WorkOrderTaskAssignedEvent event) {
        // Synchronous dispatch / push alert to mechanic mobile app
    }

    @EventListener
    public void on(WorkOrderTaskHoldEvent event) {
        // High-priority alert to workshop inventory / parts desk
    }

    @EventListener
    public void on(WorkOrderTaskResumedEvent event) {
        // Restores active wrench time monitoring
    }

    @EventListener
    public void on(WorkOrderTaskCompletedEvent event) {
        // Records technical labor productivity metrics
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProductStockReservationRequestedEvent event) {
        applicationEventPublisher.publishEvent(new ProductStockReservationRequestedIntegrationEvent(
                event.workOrderId().value(),
                event.taskId().value(),
                event.productId(),
                event.quantity().value(),
                event.occurredOn()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProductStockReservationCancelledEvent event) {
        applicationEventPublisher.publishEvent(new ProductStockReservationCancelledIntegrationEvent(
                event.workOrderId().value(),
                event.taskId().value(),
                event.productId(),
                event.quantity().value(),
                event.occurredOn()
        ));
    }
}
