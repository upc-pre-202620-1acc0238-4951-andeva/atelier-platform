package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.LocalDate;
import java.util.Objects;

public record TransferVehicleOwnershipCommand(
        TenantId tenantId,
        VehicleId vehicleId,
        CustomerId newOwnerId,
        LocalDate transferDate
) {
    public TransferVehicleOwnershipCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(newOwnerId, "New owner cannot be null");
        Objects.requireNonNull(transferDate, "Transfer date cannot be null");
    }
}
