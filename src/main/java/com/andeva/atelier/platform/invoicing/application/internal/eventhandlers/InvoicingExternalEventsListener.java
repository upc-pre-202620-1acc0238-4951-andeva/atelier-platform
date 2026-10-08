package com.andeva.atelier.platform.invoicing.application.internal.eventhandlers;

import com.andeva.atelier.platform.iot.interfaces.events.WorkOrderCompletedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * External Integration Event Listener receiving business notifications
 * from collaborating bounded contexts (Workshop Operations MRO, Inventory, Human Resources)
 * to coordinate billing regularizations and operational expense aggregation.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class InvoicingExternalEventsListener {

    private static final Logger log = LoggerFactory.getLogger(InvoicingExternalEventsListener.class);

    public InvoicingExternalEventsListener() {
        log.info("Initialized InvoicingExternalEventsListener for cross-context collaboration");
    }

    /**
     * Handles workshop work order completion events to prepare billing regularizations.
     * (Retained for backward-compatibility)
     *
     * @param event the completed work order event from IoT
     */
    @EventListener
    public void on(WorkOrderCompletedIntegrationEvent event) {
        log.info("Received legacy WorkOrderCompletedIntegrationEvent for work order: {} (tenant: {}, vehicle: {}). Ready for fiscal invoicing.",
                event.workOrderId(), event.tenantId(), event.vehicleId());
    }

    /**
     * Handles real operations workshop work order completion events to prepare billing regularizations.
     *
     * @param event the completed work order event from Operations MRO
     */
    @EventListener
    public void on(com.andeva.atelier.platform.operations.interfaces.events.WorkOrderCompletedIntegrationEvent event) {
        log.info("Received real Operations WorkOrderCompletedIntegrationEvent for work order: {} (tenant: {}, vehicle: {}). Ready for fiscal invoicing.",
                event.workOrderId(), event.tenantId(), event.vehicleId());
    }
}
