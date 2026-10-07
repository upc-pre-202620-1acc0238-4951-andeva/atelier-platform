package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.DeviceInstallationResponse;
import org.springframework.stereotype.Component;

/**
 * Transforms {@link DeviceInstallation} aggregate roots into REST {@link DeviceInstallationResponse} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class DeviceInstallationResourceAssembler {

    public DeviceInstallationResponse toResponse(DeviceInstallation installation) {
        if (installation == null) {
            return null;
        }

        return new DeviceInstallationResponse(
                installation.getId().value(),
                installation.getDeviceId().value(),
                installation.getVehicleId().value(),
                installation.getTenantId().value(),
                installation.getInstalledAt(),
                installation.getUninstalledAt().orElse(null),
                installation.getInitialOdometerKm(),
                installation.getFinalOdometerKm().orElse(null),
                installation.isActive()
        );
    }
}
