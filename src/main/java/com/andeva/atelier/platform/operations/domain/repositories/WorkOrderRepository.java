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

public interface WorkOrderRepository {

    WorkOrder save(WorkOrder workOrder);

    Optional<WorkOrder> findById(WorkOrderId id);

    Optional<WorkOrder> findByOrderNumber(WorkOrderNumber orderNumber);

    List<WorkOrder> findByTenantId(TenantId tenantId);

    List<WorkOrder> findByBranchId(TenantId tenantId, BranchId branchId);

    List<WorkOrder> findByVehicleId(VehicleId vehicleId);

    List<WorkOrder> findByCustomerId(CustomerId customerId);

    Optional<WorkOrder> findByCurrentBayId(WorkBayId bayId);

    Integer findNextInternalSequence(TenantId tenantId);
}
