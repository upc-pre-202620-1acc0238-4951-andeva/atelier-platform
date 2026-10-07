package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.events.Obd2DeviceBrokenEvent;
import com.andeva.atelier.platform.iot.domain.model.events.Obd2DeviceLostEvent;
import com.andeva.atelier.platform.iot.domain.model.events.Obd2DeviceRegisteredEvent;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for the Obd2Device aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
class Obd2DeviceAggregateTest {

    @Test
    @DisplayName("register() factory method should initialize active device and emit Obd2DeviceRegisteredEvent")
    void testRegisterDevice() {
        TenantId tenantId = TenantId.generate();
        DeviceIdentifier identifier = DeviceIdentifier.of("00:1A:7D:DA:71:13");

        Obd2Device device = Obd2Device.register(
                tenantId,
                identifier,
                ConnectionType.BLUETOOTH_BLE,
                "ELM327 v2.1",
                "1.0.0"
        );

        assertThat(device.getId()).isNotNull();
        assertThat(device.getTenantId()).isEqualTo(tenantId);
        assertThat(device.getDeviceIdentifier()).isEqualTo(identifier);
        assertThat(device.getConnectionType()).isEqualTo(ConnectionType.BLUETOOTH_BLE);
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(device.isOperational()).isTrue();
        assertThat(device.getHardwareModel()).isEqualTo("ELM327 v2.1");
        assertThat(device.getFirmwareVersion()).isEqualTo("1.0.0");

        assertThat(device.domainEvents()).hasSize(1);
        assertThat(device.domainEvents().iterator().next()).isInstanceOf(Obd2DeviceRegisteredEvent.class);

        Obd2DeviceRegisteredEvent event = (Obd2DeviceRegisteredEvent) device.domainEvents().iterator().next();
        assertThat(event.deviceId()).isEqualTo(device.getId());
        assertThat(event.tenantId()).isEqualTo(tenantId);
        assertThat(event.identifier()).isEqualTo(identifier);
    }

    @Test
    @DisplayName("markLost() should transition status to LOST and emit Obd2DeviceLostEvent")
    void testMarkLost() {
        Obd2Device device = Obd2Device.register(
                TenantId.generate(),
                DeviceIdentifier.of("00:1A:7D:DA:71:13"),
                ConnectionType.BLUETOOTH_BLE,
                "ELM327",
                "1.0.0"
        );
        device.clearDomainEvents();

        device.markLost();

        assertThat(device.getStatus()).isEqualTo(DeviceStatus.LOST);
        assertThat(device.isOperational()).isFalse();
        assertThat(device.domainEvents()).hasSize(1);
        assertThat(device.domainEvents().iterator().next()).isInstanceOf(Obd2DeviceLostEvent.class);
    }

    @Test
    @DisplayName("markBroken() should transition status to BROKEN and emit Obd2DeviceBrokenEvent")
    void testMarkBroken() {
        Obd2Device device = Obd2Device.register(
                TenantId.generate(),
                DeviceIdentifier.of("358240051111110"),
                ConnectionType.SIM_CELLULAR,
                "Teltonika FMB920",
                "2.3.1"
        );
        device.clearDomainEvents();

        device.markBroken();

        assertThat(device.getStatus()).isEqualTo(DeviceStatus.BROKEN);
        assertThat(device.isOperational()).isFalse();
        assertThat(device.domainEvents()).hasSize(1);
        assertThat(device.domainEvents().iterator().next()).isInstanceOf(Obd2DeviceBrokenEvent.class);
    }

    @Test
    @DisplayName("updateFirmware and state transitions")
    void testFirmwareAndStatus() {
        Obd2Device device = Obd2Device.register(
                TenantId.generate(),
                DeviceIdentifier.of("00:1A:7D:DA:71:13"),
                ConnectionType.BLUETOOTH_BLE,
                "ELM327",
                "1.0.0"
        );

        device.updateFirmware("1.1.0");
        assertThat(device.getFirmwareVersion()).isEqualTo("1.1.0");

        device.markInactive();
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE);
        assertThat(device.isOperational()).isFalse();

        device.markActive();
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(device.isOperational()).isTrue();
    }
}
