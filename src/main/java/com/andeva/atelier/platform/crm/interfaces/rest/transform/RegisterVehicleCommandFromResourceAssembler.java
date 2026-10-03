package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateVehicleResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Assembler creating RegisterVehicleCommand from REST resource.
 *
 * @author Adiel Sanchez Santin
 */
public final class RegisterVehicleCommandFromResourceAssembler {

    private RegisterVehicleCommandFromResourceAssembler() {
    }

    public static RegisterVehicleCommand toCommandFromResource(UUID tenantId, CreateVehicleResource resource) {
        EngineType engineType = EngineType.valueOf(resource.engineType().trim().toUpperCase(Locale.ROOT));

        Optional<CustomerId> customerId = resource.customerId() != null
                ? Optional.of(CustomerId.of(resource.customerId()))
                : Optional.empty();

        Optional<UserId> userId = resource.userId() != null
                ? Optional.of(UserId.of(resource.userId()))
                : Optional.empty();

        return new RegisterVehicleCommand(
                TenantId.of(tenantId),
                resource.plate().trim(),
                resource.vin() != null ? resource.vin().trim() : null,
                resource.brand().trim(),
                resource.model().trim(),
                resource.year(),
                engineType,
                customerId,
                userId
        );
    }
}
