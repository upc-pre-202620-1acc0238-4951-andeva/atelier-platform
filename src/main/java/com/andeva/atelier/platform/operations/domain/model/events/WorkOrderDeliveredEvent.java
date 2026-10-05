package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record WorkOrderDeliveredEvent(
        WorkOrderId workOrderId,
        TenantId tenantId,
        VehicleId vehicleId,
        Instant occurredOn
) implements Serializable {
    public WorkOrderDeliveredEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderDeliveredEvent of(WorkOrderId workOrderId, TenantId tenantId, VehicleId vehicleId) {
        return new WorkOrderDeliveredEvent(workOrderId, tenantId, vehicleId, Instant.now());
    }
}
