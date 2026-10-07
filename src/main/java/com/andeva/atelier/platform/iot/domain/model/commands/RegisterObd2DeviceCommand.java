package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain command to register a new OBD-II telematics scanner in the workshop inventory.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterObd2DeviceCommand(
        TenantId tenantId,
        DeviceIdentifier identifier,
        ConnectionType type,
        String model,
        String firmware
) implements Serializable {

    public RegisterObd2DeviceCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(identifier, "DeviceIdentifier cannot be null");
        Objects.requireNonNull(type, "ConnectionType cannot be null");
        model = model != null ? model.trim() : "Generic OBD-II";
        firmware = firmware != null ? firmware.trim() : "1.0.0";
    }
}
