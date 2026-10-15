package com.andeva.atelier.platform.iot.domain.model.enums;

/**
 * Lifecycle states of a predictive maintenance alert.
 *
 * @author Joel Huamani Estefanero
 */
public enum AlertStatus {
    DISPATCHED,
    ACKNOWLEDGED,
    RESOLVED,
    DISMISSED;

    /**
     * An alert is open while it still awaits resolution (neither resolved nor dismissed).
     */
    public boolean isOpen() {
        return this == DISPATCHED || this == ACKNOWLEDGED;
    }
}
