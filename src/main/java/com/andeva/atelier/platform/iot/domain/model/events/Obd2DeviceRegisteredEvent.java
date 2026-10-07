package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a new OBD-II telemetry scanner hardware is cataloged into the workshop inventory.
 *
 * @author Joel Huamani Estefanero
 */
public record Obd2DeviceRegisteredEvent(
        DeviceId deviceId,
        TenantId tenantId,
        DeviceIdentifier identifier,
        Instant occurredOn
) implements Serializable {

    public Obd2DeviceRegisteredEvent {
        Objects.requireNonNull(deviceId, "DeviceId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(identifier, "DeviceIdentifier cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static Obd2DeviceRegisteredEvent of(DeviceId deviceId, TenantId tenantId, DeviceIdentifier identifier) {
        return new Obd2DeviceRegisteredEvent(deviceId, tenantId, identifier, Instant.now());
    }
}
