package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;

/**
 * Vehicle battery and alternator electrical potential reading in Volts (V).
 * Enforces plausible automotive electrical system range [0.0V, 30.0V].
 *
 * @author Joel Huamani Estefanero
 */
public record BatteryVoltage(double volts) implements Serializable {

    public static final double MIN_VOLTS = 0.0;
    public static final double MAX_VOLTS = 30.0;
    public static final double LOW_BATTERY_THRESHOLD = 11.8;
    public static final double CRITICAL_DISCHARGE_THRESHOLD = 10.5;
    public static final double CHARGING_MIN_VOLTS = 13.5;
    public static final double CHARGING_MAX_VOLTS = 14.8;
    public static final double OVERCHARGING_THRESHOLD = 15.0;

    public BatteryVoltage {
        if (Double.isNaN(volts) || volts < MIN_VOLTS || volts > MAX_VOLTS) {
            throw new IllegalArgumentException(
                    String.format("Battery voltage must be between %.1fV and %.1fV: %.2f", MIN_VOLTS, MAX_VOLTS, volts)
            );
        }
    }

    public static BatteryVoltage of(double volts) {
        return new BatteryVoltage(volts);
    }

    public boolean isLowBattery() {
        return volts < LOW_BATTERY_THRESHOLD;
    }

    public boolean indicatesLowBattery() {
        return isLowBattery();
    }

    public boolean isCriticalDischarge() {
        return volts < CRITICAL_DISCHARGE_THRESHOLD;
    }

    public boolean isAlternatorCharging() {
        return volts >= CHARGING_MIN_VOLTS && volts <= CHARGING_MAX_VOLTS;
    }

    public boolean isOvercharging() {
        return volts > OVERCHARGING_THRESHOLD;
    }

    @Override
    public String toString() {
        return String.format("%.2fV", volts);
    }
}
