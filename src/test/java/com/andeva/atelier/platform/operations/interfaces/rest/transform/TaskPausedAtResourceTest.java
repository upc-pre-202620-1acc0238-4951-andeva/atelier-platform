package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.application.queryservices.AssignedTaskView;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("H3: task resources carry the moment the running pause started")
class TaskPausedAtResourceTest {

    private WorkOrder order;
    private WorkOrderTask task;

    @BeforeEach
    void setUp() {
        order = WorkOrder.create(TenantId.of(UUID.randomUUID()), BranchId.of(UUID.randomUUID()), null,
                VehicleId.of(UUID.randomUUID()), CustomerId.of(UUID.randomUUID()), WorkOrderNumber.of(1),
                Mileage.of(1000), DiagnosticSummary.of("Revision del sistema de refrigeracion"));
        task = order.addTask(ServiceId.of(UUID.randomUUID()), UUID.randomUUID(), "Revisar termostato",
                Money.soles(new BigDecimal("80.00")), LaborHours.of(new BigDecimal("1.5")));
        task.start();
    }

    @Test
    @DisplayName("A paused task exposes pausedAt in the task detail and in the mechanic's tray")
    void shouldExposePausedAtWhilePaused() {
        task.pause(HoldReason.BREAK);

        assertThat(task.getPausedAt()).isPresent();
        assertThat(WorkOrderTaskResourceAssembler.toResourceFromEntity(task, "Service", null).pausedAt())
                .isEqualTo(task.getPausedAt().get());
        assertThat(AssignedTaskResourceAssembler.toResource(new AssignedTaskView(order, task)).pausedAt())
                .isEqualTo(task.getPausedAt().get());
    }

    @Test
    @DisplayName("A running task has no pausedAt: resuming clears it")
    void shouldClearPausedAtOnResume() {
        task.pause(HoldReason.BREAK);
        task.start();

        assertThat(WorkOrderTaskResourceAssembler.toResourceFromEntity(task, "Service", null).pausedAt()).isNull();
        assertThat(AssignedTaskResourceAssembler.toResource(new AssignedTaskView(order, task)).pausedAt()).isNull();
    }
}
