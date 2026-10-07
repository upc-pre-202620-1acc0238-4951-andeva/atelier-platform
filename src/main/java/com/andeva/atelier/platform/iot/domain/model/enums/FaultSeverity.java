package com.andeva.atelier.platform.iot.domain.model.enums;

/**
 * Diagnostic trouble code (DTC) mechanical and electrical fault severity.
 *
 * @author Joel Huamani Estefanero
 */
public enum FaultSeverity {
    LOW,
    MEDIUM,
    CRITICAL;

    public boolean isCritical() {
        return this == CRITICAL;
    }
}
