package com.andeva.atelier.platform.crm.domain.model.valueobjects;

import com.andeva.atelier.platform.crm.domain.exceptions.InvalidLicensePlateException;
import com.andeva.atelier.platform.crm.domain.exceptions.InvalidVinException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LicensePlate and Vin Value Objects Unit Tests")
class LicensePlateAndVinTest {

    @Test
    @DisplayName("Should normalize license plate removing dashes and spaces")
    void shouldNormalizeLicensePlate() {
        LicensePlate plate1 = LicensePlate.of("abc-123");
        assertThat(plate1.value()).isEqualTo("ABC123");

        LicensePlate plate2 = LicensePlate.of("  X1Y-2Z3  ");
        assertThat(plate2.value()).isEqualTo("X1Y2Z3");
    }

    @Test
    @DisplayName("Should reject invalid license plates")
    void shouldRejectInvalidPlates() {
        assertThatThrownBy(() -> LicensePlate.of(""))
                .isInstanceOf(InvalidLicensePlateException.class);

        assertThatThrownBy(() -> LicensePlate.of("TOOLONGPLATE123456"))
                .isInstanceOf(InvalidLicensePlateException.class);
    }

    @Test
    @DisplayName("Should normalize and validate 17-character ISO 3779 VIN")
    void shouldValidateVin() {
        Vin vin = Vin.of("1hgcr2f83ha000000");
        assertThat(vin.value()).isEqualTo("1HGCR2F83HA000000");

        assertThatThrownBy(() -> Vin.of("SHORTVIN"))
                .isInstanceOf(InvalidVinException.class);

        assertThatThrownBy(() -> Vin.of("1HGCR2F83HA00000I")) // 'I' is forbidden in ISO 3779
                .isInstanceOf(InvalidVinException.class);
    }
}
