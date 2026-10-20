package com.andeva.atelier.platform.operations.application;

import com.andeva.atelier.platform.operations.application.internal.eventhandlers.StaffNotificationsEventHandler;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.StaffNotificationPort;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.events.TaskSuspensionRequestedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskAssignedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskPausedEvent;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff push notifications for bay events (US26, US27, US29)")
class StaffNotificationsEventHandlerTest {

    @Mock
    private StaffNotificationPort notificationPort;
    @Mock
    private WorkOrderRepository workOrderRepository;

    @Test
    @DisplayName("Notifies the assigned mechanic, and supervisors of pauses and suspension requests")
    void shouldNotifyTheRightPeople() {
        var handler = new StaffNotificationsEventHandler(notificationPort, workOrderRepository);
        TenantId tenantId = TenantId.of(UUID.randomUUID());
        UUID mechanic = UUID.randomUUID();
        WorkOrderId orderId = WorkOrderId.of(UUID.randomUUID());
        WorkOrderTaskId taskId = WorkOrderTaskId.of(UUID.randomUUID());

        handler.on(WorkOrderTaskAssignedEvent.of(orderId, taskId, mechanic));
        handler.on(WorkOrderTaskPausedEvent.of(tenantId, orderId, taskId, mechanic, HoldReason.BREAK));
        handler.on(TaskSuspensionRequestedEvent.of(tenantId, orderId, taskId, mechanic, "Pieza fisurada"));

        verify(notificationPort).notifyMember(eq(mechanic), eq("Nueva tarea asignada"), any(), anyMap());
        verify(notificationPort).notifyPermissionHolders(eq(tenantId.value()), eq("operations:tasks:hold_validate"), eq("Tarea en pausa"), any(), anyMap());
        verify(notificationPort).notifyPermissionHolders(eq(tenantId.value()), eq("operations:tasks:hold_validate"), eq("Solicitud de suspensión de tarea"), eq("Pieza fisurada"), anyMap());
    }

    @Test
    @DisplayName("A failing push never propagates to the operation")
    void shouldSwallowDeliveryFailures() {
        var handler = new StaffNotificationsEventHandler(notificationPort, workOrderRepository);
        UUID mechanic = UUID.randomUUID();
        doThrow(new RuntimeException("FCM down")).when(notificationPort).notifyMember(any(), any(), any(), anyMap());

        handler.on(WorkOrderTaskAssignedEvent.of(WorkOrderId.of(UUID.randomUUID()), WorkOrderTaskId.of(UUID.randomUUID()), mechanic));
    }
}
