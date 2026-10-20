package com.andeva.atelier.platform.operations.application.internal.eventhandlers;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.StaffNotificationPort;
import com.andeva.atelier.platform.operations.domain.model.events.ProductStockReservationRequestedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.TaskSuspensionRequestedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskAssignedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskPausedEvent;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Objects;

/**
 * Pushes bay events to the staff who must react: the assigned mechanic, supervisors and the
 * inventory desk. Notifications are best-effort and never affect the committed operation.
 */
@Component
public class StaffNotificationsEventHandler {

    private static final Logger log = LoggerFactory.getLogger(StaffNotificationsEventHandler.class);

    static final String SUPERVISOR_PERMISSION = "operations:tasks:hold_validate";
    static final String INVENTORY_AUDIT_PERMISSION = "inventory:batches:restore";

    private final StaffNotificationPort notificationPort;
    private final WorkOrderRepository workOrderRepository;

    public StaffNotificationsEventHandler(StaffNotificationPort notificationPort, WorkOrderRepository workOrderRepository) {
        this.notificationPort = Objects.requireNonNull(notificationPort, "notificationPort cannot be null");
        this.workOrderRepository = Objects.requireNonNull(workOrderRepository, "workOrderRepository cannot be null");
    }

    /** US26: the mechanic learns at once that a new task was assigned. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(WorkOrderTaskAssignedEvent event) {
        if (event.mechanicId() == null) {
            return;
        }
        safely(() -> notificationPort.notifyMember(event.mechanicId(), "Nueva tarea asignada",
                "Se te asignó una tarea en la orden de trabajo", data("TASK_ASSIGNED", event.workOrderId().value(), event.taskId().value())));
    }

    /** US27: supervisors see breaks and end-of-shift pauses. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(WorkOrderTaskPausedEvent event) {
        String body = switch (event.reason()) {
            case BREAK -> "Un mecánico pausó su tarea por refrigerio";
            case END_OF_SHIFT -> "Una tarea quedó pausada por fin de turno";
            default -> "Una tarea fue pausada";
        };
        safely(() -> notificationPort.notifyPermissionHolders(event.tenantId().value(), SUPERVISOR_PERMISSION,
                "Tarea en pausa", body, data("TASK_PAUSED", event.workOrderId().value(), event.taskId().value())));
    }

    /** US29: the administration must review a suspension request with its justification. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(TaskSuspensionRequestedEvent event) {
        safely(() -> notificationPort.notifyPermissionHolders(event.tenantId().value(), SUPERVISOR_PERMISSION,
                "Solicitud de suspensión de tarea", event.reason(),
                data("TASK_SUSPENSION_REQUESTED", event.workOrderId().value(), event.taskId().value())));
    }

    /** US23: the inventory desk audits every part charged to a task and may withdraw it. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(ProductStockReservationRequestedEvent event) {
        safely(() -> workOrderRepository.findById(event.workOrderId()).ifPresent(order ->
                notificationPort.notifyPermissionHolders(order.getTenantId().value(), INVENTORY_AUDIT_PERMISSION,
                        "Repuesto imputado en bahía",
                        "Se imputaron " + event.quantity().value().stripTrailingZeros().toPlainString()
                                + " unidad(es) a la orden #" + order.getInternalNumber().sequence(),
                        data("TASK_PART_CHARGED", event.workOrderId().value(), event.taskId().value()))));
    }

    private static Map<String, String> data(String type, Object workOrderId, Object taskId) {
        return Map.of("type", type, "workOrderId", String.valueOf(workOrderId), "taskId", String.valueOf(taskId));
    }

    private static void safely(Runnable notification) {
        try {
            notification.run();
        } catch (RuntimeException e) {
            log.warn("Staff notification could not be delivered: {}", e.getMessage());
        }
    }
}
