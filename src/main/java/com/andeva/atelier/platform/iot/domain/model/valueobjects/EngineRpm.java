package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;

/**
 * Engine crankshaft rotational speed measured in Revolutions Per Minute (RPM).
 * Enforces valid mechanical range [0, 12000].
 *
 * @author Joel Huamani Estefanero
 */
public record EngineRpm(int rpm) implements Serializable {

    public static final int MIN_RPM = 0;
    public static final int MAX_RPM = 12000;
    public static final int REDLINE_RPM = 6000;
    public static final int MIN_IDLE_RPM = 600;
    public static final int MAX_IDLE_RPM = 1000;

    public EngineRpm {
        if (rpm < MIN_RPM || rpm > MAX_RPM) {
            throw new IllegalArgumentException(
                    String.format("Engine RPM must be between %d and %d: %d", MIN_RPM, MAX_RPM, rpm)
            );
        }
    }

    public static EngineRpm of(int rpm) {
        return new EngineRpm(rpm);
    }

    public boolean isExcessiveRpm() {
        return rpm > REDLINE_RPM;
    }

    public boolean isExcessive() {
        return isExcessiveRpm();
    }

    public boolean isIdling() {
        return rpm >= MIN_IDLE_RPM && rpm <= MAX_IDLE_RPM;
    }

    public boolean isEngineStopped() {
        return rpm == 0;
    }

    @Override
    public String toString() {
        return rpm + " RPM";
    }
}
