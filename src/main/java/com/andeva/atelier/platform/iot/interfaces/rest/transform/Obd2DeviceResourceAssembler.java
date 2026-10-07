package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.Obd2DeviceResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Transforms {@link Obd2Device} aggregate roots into REST {@link Obd2DeviceResponse} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class Obd2DeviceResourceAssembler {

    public Obd2DeviceResponse toResponse(Obd2Device device) {
        if (device == null) {
            return null;
        }

        return new Obd2DeviceResponse(
                device.getId().value(),
                device.getTenantId().value(),
                device.getDeviceIdentifier().value(),
                device.getConnectionType().name(),
                device.getStatus().name(),
                device.getHardwareModel(),
                device.getFirmwareVersion(),
                null
        );
    }
}
