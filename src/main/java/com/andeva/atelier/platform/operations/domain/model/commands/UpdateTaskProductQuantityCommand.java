package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param productId the task line id, or the catalog product id of the line
 * @param changedBy user correcting the quantity (kept in the inventory audit trail; may be null)
 */
public record UpdateTaskProductQuantityCommand(
        WorkOrderTaskId taskId,
        UUID productId,
        BigDecimal newQuantity,
        UUID changedBy
) {
    public UpdateTaskProductQuantityCommand(WorkOrderTaskId taskId, UUID productId, BigDecimal newQuantity) {
        this(taskId, productId, newQuantity, null);
    }
}
