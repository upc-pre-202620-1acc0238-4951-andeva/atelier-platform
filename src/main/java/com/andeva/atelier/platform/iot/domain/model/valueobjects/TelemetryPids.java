package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable mapping of standardized SAE J1979 OBD-II Parameter IDs (PIDs)
 * and auxiliary sensor readings reported by telematics hardware firmware.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryPids(Map<String, Object> pids) implements Serializable {

    public TelemetryPids {
        pids = pids != null ? Map.copyOf(pids) : Map.of();
    }

    public static TelemetryPids of(Map<String, Object> pids) {
        return new TelemetryPids(pids);
    }

    public static TelemetryPids empty() {
        return new TelemetryPids(Collections.emptyMap());
    }

    public Optional<Object> get(String pid) {
        Objects.requireNonNull(pid, "PID key cannot be null");
        return Optional.ofNullable(pids.get(pid));
    }

    public boolean has(String pid) {
        Objects.requireNonNull(pid, "PID key cannot be null");
        return pids.containsKey(pid);
    }

    public int size() {
        return pids.size();
    }

    public boolean isEmpty() {
        return pids.isEmpty();
    }
}
