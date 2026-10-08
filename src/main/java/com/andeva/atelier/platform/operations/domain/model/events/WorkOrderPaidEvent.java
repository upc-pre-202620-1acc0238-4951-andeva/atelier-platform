package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record WorkOrderPaidEvent(
        WorkOrderId workOrderId,
        TenantId tenantId,
        Money totalAmount,
        Instant occurredOn
) implements Serializable {
    public WorkOrderPaidEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(totalAmount, "totalAmount cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderPaidEvent of(WorkOrderId workOrderId, TenantId tenantId) {
        return new WorkOrderPaidEvent(workOrderId, tenantId, Money.soles(java.math.BigDecimal.ZERO), Instant.now());
    }

    public static WorkOrderPaidEvent of(WorkOrderId workOrderId, TenantId tenantId, Money totalAmount) {
        return new WorkOrderPaidEvent(workOrderId, tenantId, totalAmount, Instant.now());
    }
}
