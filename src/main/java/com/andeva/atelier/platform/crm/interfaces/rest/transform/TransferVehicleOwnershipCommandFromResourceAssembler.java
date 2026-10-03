package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.commands.TransferVehicleOwnershipCommand;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.TransferVehicleOwnershipResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.UUID;

/**
 * Assembler creating TransferVehicleOwnershipCommand from REST resource.
 *
 * @author Adiel Sanchez Santin
 */
public final class TransferVehicleOwnershipCommandFromResourceAssembler {

    private TransferVehicleOwnershipCommandFromResourceAssembler() {
    }

    public static TransferVehicleOwnershipCommand toCommandFromResource(
            UUID tenantId,
            UUID vehicleId,
            TransferVehicleOwnershipResource resource
    ) {
        return new TransferVehicleOwnershipCommand(
                TenantId.of(tenantId),
                VehicleId.of(vehicleId),
                CustomerId.of(resource.newOwnerId()),
                resource.transferDate()
        );
    }
}
