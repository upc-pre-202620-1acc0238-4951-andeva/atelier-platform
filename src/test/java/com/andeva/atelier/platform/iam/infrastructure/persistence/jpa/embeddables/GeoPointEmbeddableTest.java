package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.embeddables;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GeoPointEmbeddable}.
 *
 * @author Joel Huamani Estefanero
 */
class GeoPointEmbeddableTest {

    @Test
    @DisplayName("Should convert between GeoPoint and GeoPointEmbeddable losslessly")
    void testConversion() {
        GeoPoint geoPoint = GeoPoint.of(-12.046374, -77.042793);
        GeoPointEmbeddable embeddable = GeoPointEmbeddable.fromGeoPoint(geoPoint);

        assertThat(embeddable).isNotNull();
        assertThat(embeddable.getLatitude()).isEqualTo(-12.046374);
        assertThat(embeddable.getLongitude()).isEqualTo(-77.042793);

        GeoPoint convertedBack = embeddable.toGeoPoint();
        assertThat(convertedBack).isNotNull();
        assertThat(convertedBack.latitude()).isEqualTo(-12.046374);
        assertThat(convertedBack.longitude()).isEqualTo(-77.042793);
    }

    @Test
    @DisplayName("Should handle null coordinates gracefully")
    void testNullHandling() {
        assertThat(GeoPointEmbeddable.fromGeoPoint(null)).isNull();

        GeoPointEmbeddable emptyEmbeddable = new GeoPointEmbeddable();
        assertThat(emptyEmbeddable.toGeoPoint()).isNull();

        GeoPointEmbeddable partialEmbeddable = new GeoPointEmbeddable(-12.0, null);
        assertThat(partialEmbeddable.toGeoPoint()).isNull();
    }

    @Test
    @DisplayName("Should respect equality and hashCode contract")
    void testEqualsAndHashCode() {
        GeoPointEmbeddable e1 = new GeoPointEmbeddable(-12.0, -77.0);
        GeoPointEmbeddable e2 = new GeoPointEmbeddable(-12.0, -77.0);
        GeoPointEmbeddable e3 = new GeoPointEmbeddable(-12.1, -77.0);

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        assertThat(e1).isNotEqualTo(e3);
    }
}
