package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.Objects;

public record ScheduleAppointmentCommand(
        TenantId tenantId,
        BranchId branchId,
        CustomerId customerId,
        VehicleId vehicleId,
        Instant scheduledAt,
        int estimatedDurationMinutes,
        String reason
) {
    public ScheduleAppointmentCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(scheduledAt, "ScheduledAt cannot be null");
    }
}
