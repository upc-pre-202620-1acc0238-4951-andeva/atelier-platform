package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.DtcCatalogId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for DtcCatalogEntry child entity.
 *
 * @author Joel Huamani Estefanero
 */
class DtcCatalogEntryTest {

    @Test
    @DisplayName("register() factory method should initialize catalog entry")
    void testRegisterCatalogEntry() {
        DtcCode code = DtcCode.of("P0300");
        DtcCatalogEntry entry = DtcCatalogEntry.register(
                code,
                DtcCategory.POWERTRAIN_P,
                "Random/Multiple Cylinder Misfire Detected",
                FaultSeverity.CRITICAL
        );

        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getCode()).isEqualTo(code);
        assertThat(entry.getCategory()).isEqualTo(DtcCategory.POWERTRAIN_P);
        assertThat(entry.getStandardDescription()).isEqualTo("Random/Multiple Cylinder Misfire Detected");
        assertThat(entry.getDefaultSeverity()).isEqualTo(FaultSeverity.CRITICAL);
        assertThat(entry.isCritical()).isTrue();
    }

    @Test
    @DisplayName("Equality based on ID and code")
    void testEquality() {
        DtcCatalogId id = DtcCatalogId.generate();
        DtcCode code = DtcCode.of("P0420");

        DtcCatalogEntry entry1 = new DtcCatalogEntry(
                id, code, DtcCategory.POWERTRAIN_P, "Catalyst System Efficiency", FaultSeverity.MEDIUM
        );
        DtcCatalogEntry entry2 = new DtcCatalogEntry(
                id, code, DtcCategory.POWERTRAIN_P, "Different description", FaultSeverity.MEDIUM
        );

        assertThat(entry1).isEqualTo(entry2);
        assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());

        DtcCatalogEntry entry3 = new DtcCatalogEntry(
                DtcCatalogId.generate(), code, DtcCategory.POWERTRAIN_P, "Catalyst System Efficiency", FaultSeverity.MEDIUM
        );
        assertThat(entry1).isNotEqualTo(entry3);
    }

    @Test
    @DisplayName("Null arguments should throw NullPointerException")
    void testNullArguments() {
        assertThatThrownBy(() -> new DtcCatalogEntry(
                null, DtcCode.of("P0100"), DtcCategory.POWERTRAIN_P, "Mass Air Flow", FaultSeverity.LOW
        )).isInstanceOf(NullPointerException.class);
    }
}
