package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.embeddables;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * JPA Embeddable mapping WGS84 geographic coordinates (latitude and longitude)
 * into relational table columns.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class GeoPointEmbeddable {

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public GeoPointEmbeddable(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Converts this JPA embeddable into a pure domain {@link GeoPoint} value object.
     *
     * @return the domain GeoPoint, or null if coordinates are not set
     */
    public GeoPoint toGeoPoint() {
        if (latitude == null || longitude == null) {
            return null;
        }
        return GeoPoint.of(latitude, longitude);
    }

    /**
     * Creates a new embeddable from a pure domain {@link GeoPoint} value object.
     *
     * @param geoPoint domain geographic point
     * @return initialized embeddable, or null if input is null
     */
    public static GeoPointEmbeddable fromGeoPoint(GeoPoint geoPoint) {
        if (geoPoint == null) {
            return null;
        }
        return new GeoPointEmbeddable(geoPoint.latitude(), geoPoint.longitude());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoPointEmbeddable that)) return false;
        return Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
