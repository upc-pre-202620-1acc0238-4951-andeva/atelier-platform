package com.andeva.atelier.platform.iot.domain.model.enums;

/**
 * Operational inventory status for OBD-II telemetry scanner hardware.
 *
 * @author Joel Huamani Estefanero
 */
public enum DeviceStatus {
    ACTIVE,
    INACTIVE,
    LOST,
    BROKEN;

    public boolean isOperational() {
        return this == ACTIVE;
    }
}
