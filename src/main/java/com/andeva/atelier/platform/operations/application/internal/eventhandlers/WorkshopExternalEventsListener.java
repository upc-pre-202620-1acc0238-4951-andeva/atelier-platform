package com.andeva.atelier.platform.operations.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.interfaces.events.AppointmentArrivedIntegrationEvent;
import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
public class WorkshopExternalEventsListener {

    private static final Logger log = LoggerFactory.getLogger(WorkshopExternalEventsListener.class);

    private final WorkOrderCommandService workOrderCommandService;

    public WorkshopExternalEventsListener(WorkOrderCommandService workOrderCommandService) {
        this.workOrderCommandService = Objects.requireNonNull(workOrderCommandService);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AppointmentArrivedIntegrationEvent event) {
        log.info("Received AppointmentArrivedIntegrationEvent for appointment: {}", event.appointmentId());
        CreateWorkOrderCommand command = new CreateWorkOrderCommand(
                TenantId.of(event.tenantId()),
                BranchId.of(event.branchId()),
                AppointmentId.of(event.appointmentId()),
                VehicleId.of(event.vehicleId()),
                CustomerId.of(event.customerId()),
                Mileage.of(0),
                DiagnosticSummary.of("Ingreso por cita programada: " + event.appointmentId())
        );
        workOrderCommandService.handle(command);
    }
}
