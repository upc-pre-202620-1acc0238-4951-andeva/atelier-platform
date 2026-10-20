package com.andeva.atelier.platform.operations.domain.model;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Task pauses (US27) and defective-part suspension workflow (US29)")
class TaskPauseAndSuspensionTest {

    private WorkOrderTask runningTask() {
        WorkOrderTask task = WorkOrderTask.create(WorkOrderId.of(UUID.randomUUID()), ServiceId.of(UUID.randomUUID()), UUID.randomUUID(),
                "Cambio de embrague", Money.soles(new BigDecimal("300.00")), LaborHours.of(BigDecimal.valueOf(3)));
        task.start();
        return task;
    }

    @Test
    @DisplayName("A break pause is lifted by the mechanic with start()")
    void shouldPauseAndResumeBreak() {
        WorkOrderTask task = runningTask();

        task.pause(HoldReason.BREAK);
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.ON_HOLD);
        assertThat(task.getPausedAt()).isPresent();

        task.start();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.IN_PROGRESS);
        assertThat(task.getHoldReason()).isEmpty();
        assertThat(task.getPausedAt()).isEmpty();
    }

    @Test
    @DisplayName("A suspension request keeps the task running until approved, and only resume() lifts it")
    void shouldRequireApprovalForSuspension() {
        WorkOrderTask task = runningTask();

        task.requestSuspension("Disco de embrague nuevo llegó fisurado", UUID.randomUUID());
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.IN_PROGRESS);
        assertThat(task.hasPendingSuspensionRequest()).isTrue();

        task.approveSuspension();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.ON_HOLD);
        assertThat(task.getHoldReason()).contains(HoldReason.DEFECTIVE_PART);
        assertThat(task.hasPendingSuspensionRequest()).isFalse();

        assertThatThrownBy(task::start).isInstanceOf(IllegalStateException.class);
        task.resume();
        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("A rejected request leaves the task in progress")
    void shouldStayInProgressWhenRejected() {
        WorkOrderTask task = runningTask();
        task.requestSuspension("Pieza dudosa", UUID.randomUUID());

        task.rejectSuspension();

        assertThat(task.getStatus()).isEqualTo(WorkOrderTaskStatus.IN_PROGRESS);
        assertThat(task.hasPendingSuspensionRequest()).isFalse();
        assertThatThrownBy(() -> task.pause(HoldReason.DEFECTIVE_PART)).isInstanceOf(IllegalArgumentException.class);
    }
}
