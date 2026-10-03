package com.andeva.atelier.platform.shared.domain.model.valueobjects;

/**
 * Measurement units for stock control, automotive parts, fluids, and labor hours.
 *
 * @author Joel Huamani Estefanero
 */
public enum MeasurementUnit {
    UNIT("Unit"),
    LITER("Liter"),
    GALLON("Gallon"),
    KILOGRAM("Kilogram"),
    METER("Meter"),
    HOUR("Labor Hour");

    private final String description;

    MeasurementUnit(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
