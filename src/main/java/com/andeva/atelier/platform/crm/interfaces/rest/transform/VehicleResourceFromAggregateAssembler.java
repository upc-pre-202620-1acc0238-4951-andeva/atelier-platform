package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.VehicleResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;

import java.util.Objects;
import java.util.UUID;

/**
 * Assembler transforming Vehicle domain aggregates into VehicleResource REST responses.
 *
 * @author Adiel Sanchez Santin
 */
public final class VehicleResourceFromAggregateAssembler {

    private VehicleResourceFromAggregateAssembler() {
    }

    public static VehicleResource toResourceFromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        UUID currentOwnerId = vehicle.getActiveOwnership()
                .map(VehicleOwnership::getCustomerId)
                .filter(Objects::nonNull)
                .map(CustomerId::value)
                .orElse(null);

        return new VehicleResource(
                vehicle.id().value(),
                vehicle.plate().value(),
                vehicle.vin() != null ? vehicle.vin().value() : null,
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.engineType().name(),
                currentOwnerId
        );
    }
}
