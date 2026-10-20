package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.util.UUID;

/**
 * Head mechanic's request to suspend a task for a defective or missing part; it waits for
 * administrative approval while the task keeps running.
 */
public record HoldWorkOrderTaskCommand(
        WorkOrderTaskId taskId,
        String missingItemDescription,
        UUID inventoryItemId,
        UUID requestedByUserId
) {
    public HoldWorkOrderTaskCommand(WorkOrderTaskId taskId, String missingItemDescription, UUID inventoryItemId) {
        this(taskId, missingItemDescription, inventoryItemId, null);
    }
}
