package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for all IoT JPA Attribute Converters.
 *
 * @author Joel Huamani Estefanero
 */
class IoTJpaConvertersTest {

    @Test
    @DisplayName("AlertSeverityConverter should correctly convert to and from database column")
    void testAlertSeverityConverter() {
        AlertSeverityConverter converter = new AlertSeverityConverter();

        assertThat(converter.convertToDatabaseColumn(AlertSeverity.CRITICAL)).isEqualTo("CRITICAL");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("CRITICAL")).isEqualTo(AlertSeverity.CRITICAL);
        assertThat(converter.convertToEntityAttribute("high")).isEqualTo(AlertSeverity.HIGH);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();
    }

    @Test
    @DisplayName("AlertStatusConverter should correctly convert to and from database column")
    void testAlertStatusConverter() {
        AlertStatusConverter converter = new AlertStatusConverter();

        assertThat(converter.convertToDatabaseColumn(AlertStatus.DISPATCHED)).isEqualTo("DISPATCHED");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("DISPATCHED")).isEqualTo(AlertStatus.DISPATCHED);
        assertThat(converter.convertToEntityAttribute("acknowledged")).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
    }

    @Test
    @DisplayName("AlertTypeConverter should correctly convert to and from database column")
    void testAlertTypeConverter() {
        AlertTypeConverter converter = new AlertTypeConverter();

        assertThat(converter.convertToDatabaseColumn(AlertType.ENGINE_OVERHEATING_RISK))
                .isEqualTo("ENGINE_OVERHEATING_RISK");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("BATTERY_FAILURE_RISK"))
                .isEqualTo(AlertType.BATTERY_FAILURE_RISK);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("ConnectionTypeConverter should correctly convert to and from database column")
    void testConnectionTypeConverter() {
        ConnectionTypeConverter converter = new ConnectionTypeConverter();

        assertThat(converter.convertToDatabaseColumn(ConnectionType.BLUETOOTH_BLE)).isEqualTo("BLUETOOTH_BLE");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("SIM_CELLULAR")).isEqualTo(ConnectionType.SIM_CELLULAR);
        assertThat(converter.convertToEntityAttribute("wifi")).isEqualTo(ConnectionType.WIFI);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("DeviceStatusConverter should correctly convert to and from database column")
    void testDeviceStatusConverter() {
        DeviceStatusConverter converter = new DeviceStatusConverter();

        assertThat(converter.convertToDatabaseColumn(DeviceStatus.ACTIVE)).isEqualTo("ACTIVE");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("BROKEN")).isEqualTo(DeviceStatus.BROKEN);
        assertThat(converter.convertToEntityAttribute("lost")).isEqualTo(DeviceStatus.LOST);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("DtcCategoryConverter should correctly convert to and from database column")
    void testDtcCategoryConverter() {
        DtcCategoryConverter converter = new DtcCategoryConverter();

        assertThat(converter.convertToDatabaseColumn(DtcCategory.POWERTRAIN_P)).isEqualTo("POWERTRAIN_P");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("CHASSIS_C")).isEqualTo(DtcCategory.CHASSIS_C);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("FaultSeverityConverter should correctly convert to and from database column")
    void testFaultSeverityConverter() {
        FaultSeverityConverter converter = new FaultSeverityConverter();

        assertThat(converter.convertToDatabaseColumn(FaultSeverity.CRITICAL)).isEqualTo("CRITICAL");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("MEDIUM")).isEqualTo(FaultSeverity.MEDIUM);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("RiskLevelConverter should correctly convert to and from database column")
    void testRiskLevelConverter() {
        RiskLevelConverter converter = new RiskLevelConverter();

        assertThat(converter.convertToDatabaseColumn(RiskLevel.CRITICAL)).isEqualTo("CRITICAL");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();

        assertThat(converter.convertToEntityAttribute("MODERATE")).isEqualTo(RiskLevel.MODERATE);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
