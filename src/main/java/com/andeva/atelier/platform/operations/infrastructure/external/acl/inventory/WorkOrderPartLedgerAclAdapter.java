package com.andeva.atelier.platform.operations.infrastructure.external.acl.inventory;

import com.andeva.atelier.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.WorkOrderPartLedgerAclService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Anti-Corruption Layer adapter reading the part allocations Inventory keeps for each work order.
 */
@Component
public class WorkOrderPartLedgerAclAdapter implements WorkOrderPartLedgerAclService {

    private final InventoryContextFacade inventoryContextFacade;

    public WorkOrderPartLedgerAclAdapter(@Lazy InventoryContextFacade inventoryContextFacade) {
        this.inventoryContextFacade = Objects.requireNonNull(inventoryContextFacade, "inventoryContextFacade cannot be null");
    }

    @Override
    public List<PartCost> costsOf(UUID workOrderId) {
        return inventoryContextFacade.getPartCostsOfWorkOrder(workOrderId).stream()
                .map(c -> new PartCost(c.itemId(), c.taskId(), c.batchId(), c.quantity(), c.unitCost(), c.totalCost(), c.currency()))
                .toList();
    }

    @Override
    public List<PartMovement> movementsOf(UUID workOrderId) {
        return inventoryContextFacade.getPartMovementsOfWorkOrder(workOrderId).stream()
                .map(m -> new PartMovement(m.allocationId(), m.itemId(), m.taskId(), m.batchId(), m.chargedQuantity(),
                        m.returnedQuantity(), m.unitCost(), m.currency(), m.chargedAt(), m.lastReturnedAt(),
                        m.lastReturnedBy(), m.lastReturnReason()))
                .toList();
    }
}
