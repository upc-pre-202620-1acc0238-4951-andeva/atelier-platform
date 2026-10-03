package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.VehicleOwnershipResource;

import java.util.UUID;

/**
 * Assembler transforming VehicleOwnership entities into VehicleOwnershipResource REST responses.
 *
 * @author Adiel Sanchez Santin
 */
public final class VehicleOwnershipResourceFromEntityAssembler {

    private VehicleOwnershipResourceFromEntityAssembler() {
    }

    public static VehicleOwnershipResource toResourceFromEntity(VehicleOwnership ownership) {
        if (ownership == null) {
            return null;
        }

        UUID vehicleId = ownership.getVehicleId() != null ? ownership.getVehicleId().value() : null;
        UUID customerId = ownership.getCustomerId() != null ? ownership.getCustomerId().value() : null;
        UUID userId = ownership.getUserId() != null ? ownership.getUserId().value() : null;

        return new VehicleOwnershipResource(
                ownership.getId().value(),
                vehicleId,
                customerId,
                userId,
                ownership.getStartDate(),
                ownership.getEndDate()
        );
    }
}
