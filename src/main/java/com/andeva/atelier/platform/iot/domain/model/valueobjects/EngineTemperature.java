package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;

/**
 * Engine coolant temperature reading measured in degrees Celsius (°C).
 * Enforces physically plausible thermodynamic boundaries [-40.0°C, 200.0°C].
 *
 * @author Joel Huamani Estefanero
 */
public record EngineTemperature(double celsius) implements Serializable {

    public static final double MIN_CELSIUS = -40.0;
    public static final double MAX_CELSIUS = 200.0;
    public static final double CRITICAL_OVERHEATING_THRESHOLD = 105.0;
    public static final double SEVERE_OVERHEATING_THRESHOLD = 115.0;

    public EngineTemperature {
        if (Double.isNaN(celsius) || celsius < MIN_CELSIUS || celsius > MAX_CELSIUS) {
            throw new IllegalArgumentException(
                    String.format("Engine temperature must be between %.1f°C and %.1f°C: %.2f", MIN_CELSIUS, MAX_CELSIUS, celsius)
            );
        }
    }

    public static EngineTemperature of(double celsius) {
        return new EngineTemperature(celsius);
    }

    public boolean isCriticalOverheating() {
        return celsius >= CRITICAL_OVERHEATING_THRESHOLD;
    }

    public boolean indicatesOverheating() {
        return isCriticalOverheating();
    }

    public boolean isSevereOverheating() {
        return celsius >= SEVERE_OVERHEATING_THRESHOLD;
    }

    public boolean isNormalOperatingTemperature() {
        return celsius >= 85.0 && celsius <= 100.0;
    }

    public boolean isColdEngine() {
        return celsius < 60.0;
    }

    public double toFahrenheit() {
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    @Override
    public String toString() {
        return String.format("%.1f°C", celsius);
    }
}
