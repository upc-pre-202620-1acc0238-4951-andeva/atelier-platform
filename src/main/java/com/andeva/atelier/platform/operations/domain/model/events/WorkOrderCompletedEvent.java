package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record WorkOrderCompletedEvent(
        WorkOrderId workOrderId,
        TenantId tenantId,
        VehicleId vehicleId,
        Money totalAmount,
        Instant occurredOn
) implements Serializable {
    public WorkOrderCompletedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(totalAmount, "totalAmount cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderCompletedEvent of(WorkOrderId workOrderId, TenantId tenantId, VehicleId vehicleId, Money totalAmount) {
        return new WorkOrderCompletedEvent(workOrderId, tenantId, vehicleId, totalAmount, Instant.now());
    }
}
