package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;
import java.util.Optional;

public record RegisterVehicleCommand(
        TenantId tenantId,
        String plate,
        String vin,
        String brand,
        String model,
        int year,
        EngineType engineType,
        Optional<CustomerId> customerId,
        Optional<UserId> userId
) {
    public RegisterVehicleCommand {
        Objects.requireNonNull(plate, "Plate cannot be null");
        Objects.requireNonNull(brand, "Brand cannot be null");
        Objects.requireNonNull(model, "Model cannot be null");
        Objects.requireNonNull(engineType, "EngineType cannot be null");
        customerId = customerId != null ? customerId : Optional.empty();
        userId = userId != null ? userId : Optional.empty();
    }
}
