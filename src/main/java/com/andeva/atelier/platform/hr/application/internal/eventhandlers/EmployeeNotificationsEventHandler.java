package com.andeva.atelier.platform.hr.application.internal.eventhandlers;

import com.andeva.atelier.platform.hr.application.internal.outbound.acl.EmployeeNotificationPort;
import com.andeva.atelier.platform.hr.domain.model.enums.JustificationStatus;
import com.andeva.atelier.platform.hr.domain.model.events.AttendanceJustificationReviewedEvent;
import com.andeva.atelier.platform.hr.domain.model.events.OvertimeRecordedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Objects;

/**
 * Tells employees the outcome of HR decisions that concern them (best-effort pushes).
 */
@Component
public class EmployeeNotificationsEventHandler {

    private static final Logger log = LoggerFactory.getLogger(EmployeeNotificationsEventHandler.class);

    static final String PAYROLL_PERMISSION = "hr:payroll:manage";

    private final EmployeeNotificationPort notificationPort;

    public EmployeeNotificationsEventHandler(EmployeeNotificationPort notificationPort) {
        this.notificationPort = Objects.requireNonNull(notificationPort, "notificationPort cannot be null");
    }

    /** US10: verdict of the lateness justification. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(AttendanceJustificationReviewedEvent event) {
        boolean approved = event.verdict() == JustificationStatus.APPROVED;
        try {
            notificationPort.notifyEmployee(event.membershipId().value(),
                    approved ? "Descargo aprobado" : "Descargo desestimado",
                    approved ? "Tu tardanza fue justificada por la administración"
                            : "La administración desestimó tu descargo de tardanza",
                    Map.of("type", "JUSTIFICATION_REVIEWED", "attendanceId", event.attendanceRecordId().value().toString(),
                            "verdict", event.verdict().name()));
        } catch (RuntimeException e) {
            log.warn("Justification verdict notification could not be delivered: {}", e.getMessage());
        }
    }

    /** US08 E2: overtime is flagged to whoever settles the payroll. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(OvertimeRecordedEvent event) {
        try {
            notificationPort.notifyPermissionHolders(event.tenantId().value(), PAYROLL_PERMISSION,
                    "Sobretiempo registrado",
                    "Un colaborador registró " + event.overtimeMinutes() + " min de sobretiempo para la planilla",
                    Map.of("type", "OVERTIME_RECORDED", "attendanceId", event.attendanceRecordId().value().toString(),
                            "membershipId", event.membershipId().value().toString(),
                            "overtimeMinutes", String.valueOf(event.overtimeMinutes())));
        } catch (RuntimeException e) {
            log.warn("Overtime notification could not be delivered: {}", e.getMessage());
        }
    }
}
