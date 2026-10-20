package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;
import java.util.UUID;

/**
 * Stops the labor clock of every running task of a mechanic who clocked out.
 */
public record PauseTasksOnClockOutCommand(
        TenantId tenantId,
        UUID mechanicMembershipId
) {
    public PauseTasksOnClockOutCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(mechanicMembershipId, "mechanicMembershipId cannot be null");
    }
}
