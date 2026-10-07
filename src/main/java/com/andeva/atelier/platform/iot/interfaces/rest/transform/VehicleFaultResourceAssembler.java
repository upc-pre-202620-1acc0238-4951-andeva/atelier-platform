package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.VehicleFaultResponse;
import org.springframework.stereotype.Component;

/**
 * Transforms {@link VehicleFault} aggregate roots into REST {@link VehicleFaultResponse} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VehicleFaultResourceAssembler {

    public VehicleFaultResponse toResponse(VehicleFault fault) {
        if (fault == null) {
            return null;
        }

        return new VehicleFaultResponse(
                fault.getId().value(),
                fault.getVehicleId().value(),
                fault.getDtcCode().value(),
                fault.getSeverity().name(),
                fault.getDescription(),
                fault.getDetectedAt(),
                fault.isResolved(),
                fault.getResolvedAt().orElse(null)
        );
    }
}
