package com.andeva.atelier.platform.iot.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a standardized DTC Catalog master entry (SAE J2012 / ISO 15031-6).
 *
 * @author Joel Huamani Estefanero
 */
public record DtcCatalogId(UUID value) implements Serializable {

    public DtcCatalogId {
        Objects.requireNonNull(value, "DtcCatalogId value cannot be null");
    }

    public static DtcCatalogId of(UUID value) {
        return new DtcCatalogId(value);
    }

    public static DtcCatalogId of(String value) {
        Objects.requireNonNull(value, "DtcCatalogId string cannot be null");
        return new DtcCatalogId(UUID.fromString(value));
    }

    public static DtcCatalogId generate() {
        return new DtcCatalogId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
