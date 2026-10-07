package com.andeva.atelier.platform.invoicing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a Series Configuration entity.
 *
 * @author Joel Huamani Estefanero
 */
public record SeriesConfigurationId(UUID value) implements Serializable {

    public SeriesConfigurationId {
        Objects.requireNonNull(value, "Series configuration ID cannot be null");
    }

    public static SeriesConfigurationId of(UUID value) {
        return new SeriesConfigurationId(value);
    }

    public static SeriesConfigurationId of(String value) {
        Objects.requireNonNull(value, "Series configuration ID string cannot be null");
        return new SeriesConfigurationId(UUID.fromString(value));
    }

    public static SeriesConfigurationId generate() {
        return new SeriesConfigurationId(UUID.randomUUID());
    }
}
