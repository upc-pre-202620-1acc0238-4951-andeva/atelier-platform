package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record WorkShiftCreatedEvent(
        WorkShiftId workShiftId,
        TenantId tenantId,
        String name,
        Instant occurredOn
) implements Serializable {

    public WorkShiftCreatedEvent {
        Objects.requireNonNull(workShiftId, "workShiftId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkShiftCreatedEvent now(WorkShiftId workShiftId, TenantId tenantId, String name) {
        return new WorkShiftCreatedEvent(workShiftId, tenantId, name, Instant.now());
    }
}
