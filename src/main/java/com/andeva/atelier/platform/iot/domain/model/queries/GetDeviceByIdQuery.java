package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain query to look up an OBD-II device by its unique DeviceId.
 *
 * @author Joel Huamani Estefanero
 */
public record GetDeviceByIdQuery(
        DeviceId deviceId
) implements Serializable {

    public GetDeviceByIdQuery {
        Objects.requireNonNull(deviceId, "DeviceId cannot be null");
    }
}
