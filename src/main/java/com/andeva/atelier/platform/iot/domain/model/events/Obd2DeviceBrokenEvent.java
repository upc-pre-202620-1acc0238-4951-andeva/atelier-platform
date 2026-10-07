package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when an OBD-II device is diagnosed as physically or electronically broken.
 *
 * @author Joel Huamani Estefanero
 */
public record Obd2DeviceBrokenEvent(
        DeviceId deviceId,
        TenantId tenantId,
        Instant occurredOn
) implements Serializable {

    public Obd2DeviceBrokenEvent {
        Objects.requireNonNull(deviceId, "DeviceId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static Obd2DeviceBrokenEvent of(DeviceId deviceId, TenantId tenantId) {
        return new Obd2DeviceBrokenEvent(deviceId, tenantId, Instant.now());
    }
}
