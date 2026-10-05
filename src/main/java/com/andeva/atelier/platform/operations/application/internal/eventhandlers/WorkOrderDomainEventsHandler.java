package com.andeva.atelier.platform.operations.application.internal.eventhandlers;

import com.andeva.atelier.platform.operations.domain.model.events.*;
import com.andeva.atelier.platform.operations.interfaces.events.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
public class WorkOrderDomainEventsHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkOrderDomainEventsHandler(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WorkOrderCreatedEvent event) {
        applicationEventPublisher.publishEvent(new WorkOrderCreatedIntegrationEvent(
                event.workOrderId().value(),
                event.tenantId().value(),
                event.vehicleId().value(),
                event.internalNumber().sequence(),
                event.occurredOn()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WorkBayAssignedEvent event) {
        applicationEventPublisher.publishEvent(new WorkOrderBayAssignedIntegrationEvent(
                event.workOrderId().value(),
                null,
                event.bayId().value(),
                "Bay",
                event.occurredOn()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WorkOrderCompletedEvent event) {
        applicationEventPublisher.publishEvent(new WorkOrderCompletedIntegrationEvent(
                event.workOrderId().value(),
                event.tenantId().value(),
                event.vehicleId().value(),
                event.totalAmount().amount(),
                event.totalAmount().currency().name(),
                event.occurredOn()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WorkOrderPaidEvent event) {
        applicationEventPublisher.publishEvent(new WorkOrderPaidIntegrationEvent(
                event.workOrderId().value(),
                event.tenantId().value(),
                event.occurredOn()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WorkOrderDeliveredEvent event) {
        applicationEventPublisher.publishEvent(new WorkOrderDeliveredIntegrationEvent(
                event.workOrderId().value(),
                event.tenantId().value(),
                event.vehicleId().value(),
                event.occurredOn()
        ));
    }
}
