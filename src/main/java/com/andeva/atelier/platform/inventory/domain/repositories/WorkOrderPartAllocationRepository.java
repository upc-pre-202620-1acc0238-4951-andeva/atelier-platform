package com.andeva.atelier.platform.inventory.domain.repositories;

import com.andeva.atelier.platform.inventory.domain.model.entities.WorkOrderPartAllocation;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;

import java.util.List;
import java.util.UUID;

public interface WorkOrderPartAllocationRepository {

    WorkOrderPartAllocation save(WorkOrderPartAllocation allocation);

    /**
     * Allocations of an item to a work order task that still hold units, newest first (returns undo the latest charge).
     */
    List<WorkOrderPartAllocation> findOpenByWorkOrderTaskAndItem(UUID workOrderId, UUID taskId, InventoryItemId itemId);

    /** Every allocation of the work order, oldest first. */
    List<WorkOrderPartAllocation> findByWorkOrderId(UUID workOrderId);
}
