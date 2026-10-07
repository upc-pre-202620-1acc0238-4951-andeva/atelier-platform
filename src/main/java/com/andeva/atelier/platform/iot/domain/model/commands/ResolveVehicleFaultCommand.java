package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;

import java.util.Objects;

/**
 * Command to resolve an active vehicle fault with workshop technical notes.
 *
 * @param faultId         the unique fault identifier
 * @param resolutionNotes technical notes detailing corrective actions taken
 * @author Joel Huamani Estefanero
 */
public record ResolveVehicleFaultCommand(
        FaultId faultId,
        String resolutionNotes
) {
    public ResolveVehicleFaultCommand {
        Objects.requireNonNull(faultId, "FaultId cannot be null");
    }

    public ResolveVehicleFaultCommand(FaultId faultId) {
        this(faultId, null);
    }
}
