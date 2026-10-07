package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDeviceIdentifierException;
import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDtcCodeException;
import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import com.andeva.atelier.platform.iot.domain.model.ids.*;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit test suite for IoT Strongly Typed IDs and Value Objects.
 *
 * @author Joel Huamani Estefanero
 */
class IotValueObjectsAndIdsTest {

    @Nested
    @DisplayName("Strongly Typed IDs Tests")
    class StronglyTypedIdsTests {

        @Test
        @DisplayName("DeviceId should generate and parse valid UUIDs")
        void testDeviceId() {
            DeviceId id = DeviceId.generate();
            assertThat(id.value()).isNotNull();

            DeviceId parsed = DeviceId.of(id.value().toString());
            assertThat(parsed).isEqualTo(id);
            assertThat(parsed.toString()).isEqualTo(id.value().toString());

            assertThatThrownBy(() -> new DeviceId(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("DeviceId value cannot be null");
        }

        @Test
        @DisplayName("InstallationId should generate and parse valid UUIDs")
        void testInstallationId() {
            InstallationId id = InstallationId.generate();
            assertThat(id.value()).isNotNull();

            InstallationId parsed = InstallationId.of(id.value().toString());
            assertThat(parsed).isEqualTo(id);
            assertThat(parsed.toString()).isEqualTo(id.value().toString());

            assertThatThrownBy(() -> new InstallationId(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("FaultId should generate and parse valid UUIDs")
        void testFaultId() {
            FaultId id = FaultId.generate();
            assertThat(id.value()).isNotNull();

            FaultId parsed = FaultId.of(id.value().toString());
            assertThat(parsed).isEqualTo(id);
            assertThat(parsed.toString()).isEqualTo(id.value().toString());

            assertThatThrownBy(() -> new FaultId(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("AlertId should generate and parse valid UUIDs")
        void testAlertId() {
            AlertId id = AlertId.generate();
            assertThat(id.value()).isNotNull();

            AlertId parsed = AlertId.of(id.value().toString());
            assertThat(parsed).isEqualTo(id);
            assertThat(parsed.toString()).isEqualTo(id.value().toString());

            assertThatThrownBy(() -> new AlertId(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("DtcCatalogId should generate and parse valid UUIDs")
        void testDtcCatalogId() {
            DtcCatalogId id = DtcCatalogId.generate();
            assertThat(id.value()).isNotNull();

            DtcCatalogId parsed = DtcCatalogId.of(id.value().toString());
            assertThat(parsed).isEqualTo(id);
            assertThat(parsed.toString()).isEqualTo(id.value().toString());

            assertThatThrownBy(() -> new DtcCatalogId(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Shared ServiceId should generate and parse valid UUIDs")
        void testServiceId() {
            ServiceId id = ServiceId.generate();
            assertThat(id.value()).isNotNull();

            ServiceId parsed = ServiceId.of(id.value().toString());
            assertThat(parsed).isEqualTo(id);
            assertThat(parsed.toString()).isEqualTo(id.value().toString());

            com.andeva.atelier.platform.shared.domain.model.ids.ServiceId aliasId =
                    com.andeva.atelier.platform.shared.domain.model.ids.ServiceId.of(id.value());
            assertThat(aliasId.toValueObject()).isEqualTo(id);
            assertThat(com.andeva.atelier.platform.shared.domain.model.ids.ServiceId.fromValueObject(id).value())
                    .isEqualTo(id.value());
        }
    }

    @Nested
    @DisplayName("DeviceIdentifier Tests")
    class DeviceIdentifierTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "00:1A:7D:DA:71:13",
                "00-1A-7D-DA-71-13",
                "a1:b2:c3:d4:e5:f6",
                "FF:EE:DD:CC:BB:AA"
        })
        @DisplayName("Valid MAC address formats should be recognized and normalized")
        void validMacAddresses(String mac) {
            DeviceIdentifier identifier = DeviceIdentifier.of(mac);
            assertThat(identifier.isMacAddress()).isTrue();
            assertThat(identifier.isImei()).isFalse();
            assertThat(identifier.value()).isEqualTo(mac.toUpperCase().trim());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "358240051111110",
                "867530900000001",
                "123456789012345"
        })
        @DisplayName("Valid 15-digit IMEI numbers should be recognized")
        void validImeiNumbers(String imei) {
            DeviceIdentifier identifier = DeviceIdentifier.of(imei);
            assertThat(identifier.isImei()).isTrue();
            assertThat(identifier.isMacAddress()).isFalse();
            assertThat(identifier.value()).isEqualTo(imei);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid-device-id",
                "00:1A:7D:DA:71",       // too short
                "00:1A:7D:DA:71:13:99", // too long
                "00:1G:7D:DA:71:13",    // invalid hex 'G'
                "12345678901234",       // 14 digits
                "1234567890123456",     // 16 digits
                "12345678901234a"       // alphanumeric
        })
        @DisplayName("Invalid device identifiers must throw InvalidDeviceIdentifierException")
        void invalidDeviceIdentifiers(String invalid) {
            assertThatThrownBy(() -> DeviceIdentifier.of(invalid))
                    .isInstanceOf(InvalidDeviceIdentifierException.class)
                    .satisfies(ex -> {
                        InvalidDeviceIdentifierException idEx = (InvalidDeviceIdentifierException) ex;
                        assertThat(idEx.getErrorCode()).isEqualTo("ERR_INVALID_DEVICE_IDENTIFIER");
                    });
        }
    }

    @Nested
    @DisplayName("DtcCode Tests")
    class DtcCodeTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "P0300", "P0420", "P0171", "P0117", "P0217", "P1234",
                "C0001", "C0100", "B0001", "B0100", "U0100", "U0401"
        })
        @DisplayName("Valid SAE J2012 DTC codes should parse and categorize accurately")
        void validDtcCodes(String code) {
            DtcCode dtc = DtcCode.of(code);
            assertThat(dtc.value()).isEqualTo(code.toUpperCase());
            assertThat(dtc.prefix()).isEqualTo(code.charAt(0));
            assertThat(dtc.category()).isNotNull();
        }

        @Test
        @DisplayName("Generic vs Manufacturer specific classifications")
        void genericVsManufacturer() {
            DtcCode p0300 = DtcCode.of("P0300");
            assertThat(p0300.isGeneric()).isTrue();
            assertThat(p0300.isManufacturerSpecific()).isFalse();
            assertThat(p0300.category()).isEqualTo(DtcCategory.POWERTRAIN_P);

            DtcCode p1300 = DtcCode.of("P1300");
            assertThat(p1300.isGeneric()).isFalse();
            assertThat(p1300.isManufacturerSpecific()).isTrue();

            DtcCode c0001 = DtcCode.of("C0001");
            assertThat(c0001.category()).isEqualTo(DtcCategory.CHASSIS_C);

            DtcCode b0001 = DtcCode.of("B0001");
            assertThat(b0001.category()).isEqualTo(DtcCategory.BODY_B);

            DtcCode u0100 = DtcCode.of("U0100");
            assertThat(u0100.category()).isEqualTo(DtcCategory.NETWORK_U);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "P030",     // 4 chars
                "P03000",   // 6 chars
                "X0300",    // invalid prefix X
                "10300",    // numeric prefix
                "P030Z"     // Z not hex
        })
        @DisplayName("Invalid DTC codes must throw InvalidDtcCodeException")
        void invalidDtcCodes(String invalid) {
            assertThatThrownBy(() -> DtcCode.of(invalid))
                    .isInstanceOf(InvalidDtcCodeException.class)
                    .satisfies(ex -> {
                        InvalidDtcCodeException dtcEx = (InvalidDtcCodeException) ex;
                        assertThat(dtcEx.getErrorCode()).isEqualTo("ERR_INVALID_DTC_CODE");
                    });
        }
    }

    @Nested
    @DisplayName("ConfidenceScore Tests")
    class ConfidenceScoreTests {

        @Test
        @DisplayName("Valid confidence scores within [0.00, 100.00]")
        void validConfidenceScores() {
            ConfidenceScore zero = ConfidenceScore.of(0.0);
            assertThat(zero.value()).isEqualTo(new BigDecimal("0.00"));
            assertThat(zero.isHighConfidence()).isFalse();

            ConfidenceScore high = ConfidenceScore.of(88.5);
            assertThat(high.value()).isEqualTo(new BigDecimal("88.50"));
            assertThat(high.isHighConfidence()).isTrue();
            assertThat(high.isCriticalConfidence()).isFalse();

            ConfidenceScore critical = ConfidenceScore.of(98.5);
            assertThat(critical.isHighConfidence()).isTrue();
            assertThat(critical.isCriticalConfidence()).isTrue();

            ConfidenceScore max = ConfidenceScore.of(100.0);
            assertThat(max.value()).isEqualTo(new BigDecimal("100.00"));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.01, -10.0, 100.01, 150.0})
        @DisplayName("Confidence score outside [0, 100] should throw IllegalArgumentException")
        void invalidConfidenceScores(double val) {
            assertThatThrownBy(() -> ConfidenceScore.of(val))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("EngineTemperature Tests")
    class EngineTemperatureTests {

        @Test
        @DisplayName("Engine temperature domain behaviors and thresholds")
        void engineTemperatureBehaviors() {
            EngineTemperature normal = EngineTemperature.of(90.0);
            assertThat(normal.isNormalOperatingTemperature()).isTrue();
            assertThat(normal.isCriticalOverheating()).isFalse();
            assertThat(normal.isColdEngine()).isFalse();
            assertThat(normal.toFahrenheit()).isEqualTo(194.0);

            EngineTemperature cold = EngineTemperature.of(45.0);
            assertThat(cold.isColdEngine()).isTrue();

            EngineTemperature warning = EngineTemperature.of(106.0);
            assertThat(warning.isCriticalOverheating()).isTrue();
            assertThat(warning.indicatesOverheating()).isTrue();
            assertThat(warning.isSevereOverheating()).isFalse();

            EngineTemperature severe = EngineTemperature.of(118.0);
            assertThat(severe.isCriticalOverheating()).isTrue();
            assertThat(severe.isSevereOverheating()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = {-40.1, -100.0, 200.1, 500.0})
        @DisplayName("Engine temperatures out of [-40, 200] should throw IllegalArgumentException")
        void invalidTemperatures(double temp) {
            assertThatThrownBy(() -> EngineTemperature.of(temp))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("EngineRpm Tests")
    class EngineRpmTests {

        @Test
        @DisplayName("Engine RPM domain methods")
        void engineRpmBehaviors() {
            EngineRpm stopped = EngineRpm.of(0);
            assertThat(stopped.isEngineStopped()).isTrue();
            assertThat(stopped.isIdling()).isFalse();
            assertThat(stopped.isExcessiveRpm()).isFalse();

            EngineRpm idle = EngineRpm.of(800);
            assertThat(idle.isIdling()).isTrue();
            assertThat(idle.isEngineStopped()).isFalse();

            EngineRpm high = EngineRpm.of(6500);
            assertThat(high.isExcessiveRpm()).isTrue();
            assertThat(high.isExcessive()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -500, 12001, 20000})
        @DisplayName("Engine RPM out of [0, 12000] should throw IllegalArgumentException")
        void invalidRpm(int rpm) {
            assertThatThrownBy(() -> EngineRpm.of(rpm))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("VehicleSpeed Tests")
    class VehicleSpeedTests {

        @Test
        @DisplayName("Vehicle speed domain methods")
        void vehicleSpeedBehaviors() {
            VehicleSpeed stopped = VehicleSpeed.of(0);
            assertThat(stopped.isStationary()).isTrue();
            assertThat(stopped.isMoving()).isFalse();

            VehicleSpeed cruising = VehicleSpeed.of(100);
            assertThat(cruising.isMoving()).isTrue();
            assertThat(cruising.isStationary()).isFalse();
            assertThat(cruising.isExcessiveSpeed(90)).isTrue();
            assertThat(cruising.toMph()).isCloseTo(62.1371, within(0.01));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, 351, 500})
        @DisplayName("Vehicle speed out of [0, 350] should throw IllegalArgumentException")
        void invalidSpeed(int speed) {
            assertThatThrownBy(() -> VehicleSpeed.of(speed))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("BatteryVoltage Tests")
    class BatteryVoltageTests {

        @Test
        @DisplayName("Battery voltage domain evaluation")
        void batteryVoltageBehaviors() {
            BatteryVoltage normalCharging = BatteryVoltage.of(14.2);
            assertThat(normalCharging.isAlternatorCharging()).isTrue();
            assertThat(normalCharging.isLowBattery()).isFalse();
            assertThat(normalCharging.isOvercharging()).isFalse();

            BatteryVoltage low = BatteryVoltage.of(11.5);
            assertThat(low.isLowBattery()).isTrue();
            assertThat(low.indicatesLowBattery()).isTrue();
            assertThat(low.isCriticalDischarge()).isFalse();

            BatteryVoltage critical = BatteryVoltage.of(9.8);
            assertThat(critical.isLowBattery()).isTrue();
            assertThat(critical.isCriticalDischarge()).isTrue();

            BatteryVoltage overcharged = BatteryVoltage.of(15.5);
            assertThat(overcharged.isOvercharging()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, -5.0, 30.1, 50.0})
        @DisplayName("Battery voltage out of [0, 30] should throw IllegalArgumentException")
        void invalidVoltage(double volts) {
            assertThatThrownBy(() -> BatteryVoltage.of(volts))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("FuelLevel Tests")
    class FuelLevelTests {

        @Test
        @DisplayName("Fuel level domain thresholds")
        void fuelLevelBehaviors() {
            FuelLevel full = FuelLevel.of(100.0);
            assertThat(full.isLowFuel()).isFalse();
            assertThat(full.isCriticallyLow()).isFalse();

            FuelLevel low = FuelLevel.of(12.0);
            assertThat(low.isLowFuel()).isTrue();
            assertThat(low.isCriticallyLow()).isFalse();

            FuelLevel critical = FuelLevel.of(3.5);
            assertThat(critical.isLowFuel()).isTrue();
            assertThat(critical.isCriticallyLow()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, -10.0, 100.1, 150.0})
        @DisplayName("Fuel level out of [0, 100] should throw IllegalArgumentException")
        void invalidFuel(double level) {
            assertThatThrownBy(() -> FuelLevel.of(level))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("GeoCoordinates Tests")
    class GeoCoordinatesTests {

        @Test
        @DisplayName("WGS84 distance calculation between Lima and Callao")
        void distanceCalculation() {
            GeoCoordinates lima = GeoCoordinates.of(-12.046374, -77.042793);
            GeoCoordinates callao = GeoCoordinates.of(-12.056517, -77.118141);

            double distanceMeters = lima.distanceMetersTo(callao);
            assertThat(distanceMeters).isBetween(8000.0, 9000.0);
        }

        @ParameterizedTest
        @ValueSource(doubles = {-90.1, 90.1})
        @DisplayName("Invalid latitude should throw IllegalArgumentException")
        void invalidLatitude(double lat) {
            assertThatThrownBy(() -> GeoCoordinates.of(lat, 0.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(doubles = {-180.1, 180.1})
        @DisplayName("Invalid longitude should throw IllegalArgumentException")
        void invalidLongitude(double lon) {
            assertThatThrownBy(() -> GeoCoordinates.of(0.0, lon))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TelemetryPids Tests")
    class TelemetryPidsTests {

        @Test
        @DisplayName("Immutable map behavior and lookups")
        void telemetryPidsBehaviors() {
            TelemetryPids pids = TelemetryPids.of(Map.of("010C", 2500, "0105", 92));
            assertThat(pids.size()).isEqualTo(2);
            assertThat(pids.has("010C")).isTrue();
            assertThat(pids.get("010C")).contains(2500);
            assertThat(pids.has("9999")).isFalse();
            assertThat(pids.get("9999")).isEmpty();

            TelemetryPids empty = TelemetryPids.empty();
            assertThat(empty.isEmpty()).isTrue();
        }
    }
}
