package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record WorkOrderCreatedEvent(
        WorkOrderId workOrderId,
        TenantId tenantId,
        BranchId branchId,
        VehicleId vehicleId,
        CustomerId customerId,
        WorkOrderNumber internalNumber,
        Instant occurredOn
) implements Serializable {
    public WorkOrderCreatedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(internalNumber, "internalNumber cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderCreatedEvent of(WorkOrderId id, TenantId tenantId, BranchId branchId, VehicleId vehicleId, CustomerId customerId, WorkOrderNumber number) {
        return new WorkOrderCreatedEvent(id, tenantId, branchId, vehicleId, customerId, number, Instant.now());
    }
}
