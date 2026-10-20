package com.andeva.atelier.platform.operations.domain.repositories;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepository {

    WorkOrder save(WorkOrder workOrder);

    Optional<WorkOrder> findById(WorkOrderId id);

    Optional<WorkOrder> findByOrderNumber(WorkOrderNumber orderNumber);

    List<WorkOrder> findByTenantId(TenantId tenantId);

    List<WorkOrder> findByBranchId(TenantId tenantId, BranchId branchId);

    List<WorkOrder> findByVehicleId(VehicleId vehicleId);

    /**
     * Order still holding the vehicle in custody (not yet paid nor canceled), if any.
     */
    Optional<WorkOrder> findOpenByVehicleId(TenantId tenantId, VehicleId vehicleId);

    /**
     * Orders still in the workshop (draft, in progress) holding at least one task assigned to the mechanic.
     */
    List<WorkOrder> findActiveWithTasksAssignedTo(TenantId tenantId, UUID mechanicMembershipId);

    List<WorkOrder> findByCustomerId(CustomerId customerId);

    Optional<WorkOrder> findByCurrentBayId(WorkBayId bayId);

    Optional<WorkOrder> findByTaskId(com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId taskId);

    Integer findNextInternalSequence(TenantId tenantId);
}
