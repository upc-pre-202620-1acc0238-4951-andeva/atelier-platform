package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.events.DeviceInstalledOnVehicleEvent;
import com.andeva.atelier.platform.iot.domain.model.events.DeviceUninstalledFromVehicleEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for the DeviceInstallation aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
class DeviceInstallationAggregateTest {

    @Test
    @DisplayName("install() factory method should initialize active installation and emit DeviceInstalledOnVehicleEvent")
    void testInstallDevice() {
        DeviceId deviceId = DeviceId.generate();
        VehicleId vehicleId = VehicleId.generate();
        TenantId tenantId = TenantId.generate();

        DeviceInstallation installation = DeviceInstallation.install(
                deviceId,
                vehicleId,
                tenantId,
                45000
        );

        assertThat(installation.getId()).isNotNull();
        assertThat(installation.getDeviceId()).isEqualTo(deviceId);
        assertThat(installation.getVehicleId()).isEqualTo(vehicleId);
        assertThat(installation.getTenantId()).isEqualTo(tenantId);
        assertThat(installation.getInitialOdometerKm()).isEqualTo(45000);
        assertThat(installation.getUninstalledAt()).isEmpty();
        assertThat(installation.getFinalOdometerKm()).isEmpty();
        assertThat(installation.isActive()).isTrue();

        assertThat(installation.domainEvents()).hasSize(1);
        assertThat(installation.domainEvents().iterator().next()).isInstanceOf(DeviceInstalledOnVehicleEvent.class);

        DeviceInstalledOnVehicleEvent event = (DeviceInstalledOnVehicleEvent) installation.domainEvents().iterator().next();
        assertThat(event.installationId()).isEqualTo(installation.getId());
        assertThat(event.deviceId()).isEqualTo(deviceId);
        assertThat(event.vehicleId()).isEqualTo(vehicleId);
    }

    @Test
    @DisplayName("uninstall() should record final metrics, terminate session and emit DeviceUninstalledFromVehicleEvent")
    void testUninstallDevice() {
        DeviceInstallation installation = DeviceInstallation.install(
                DeviceId.generate(),
                VehicleId.generate(),
                TenantId.generate(),
                50000
        );
        installation.clearDomainEvents();

        Instant uninstalledAt = Instant.now().plus(5, ChronoUnit.HOURS);
        installation.uninstall(50250, uninstalledAt);

        assertThat(installation.isActive()).isFalse();
        assertThat(installation.getUninstalledAt()).contains(uninstalledAt);
        assertThat(installation.getFinalOdometerKm()).contains(50250);

        assertThat(installation.domainEvents()).hasSize(1);
        assertThat(installation.domainEvents().iterator().next()).isInstanceOf(DeviceUninstalledFromVehicleEvent.class);

        DeviceUninstalledFromVehicleEvent event = (DeviceUninstalledFromVehicleEvent) installation.domainEvents().iterator().next();
        assertThat(event.installationId()).isEqualTo(installation.getId());
        assertThat(event.vehicleId()).isEqualTo(installation.getVehicleId());
        assertThat(event.finalOdometerKm()).isEqualTo(50250);
    }

    @Test
    @DisplayName("uninstall() with final odometer less than initial odometer should throw IllegalArgumentException")
    void testUninstallDecreasingOdometerThrows() {
        DeviceInstallation installation = DeviceInstallation.install(
                DeviceId.generate(),
                VehicleId.generate(),
                TenantId.generate(),
                50000
        );

        assertThatThrownBy(() -> installation.uninstall(49999, Instant.now().plus(1, ChronoUnit.HOURS)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Final odometer");
    }

    @Test
    @DisplayName("uninstall() with timestamp prior to installation should throw IllegalArgumentException")
    void testUninstallPriorTimestampThrows() {
        DeviceInstallation installation = DeviceInstallation.install(
                DeviceId.generate(),
                VehicleId.generate(),
                TenantId.generate(),
                50000
        );

        Instant prior = installation.getInstalledAt().minus(1, ChronoUnit.MINUTES);
        assertThatThrownBy(() -> installation.uninstall(50100, prior))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prior to installation date");
    }

    @Test
    @DisplayName("uninstall() on already terminated installation should throw IllegalStateException")
    void testUninstallWhenAlreadyUninstalledThrows() {
        DeviceInstallation installation = DeviceInstallation.install(
                DeviceId.generate(),
                VehicleId.generate(),
                TenantId.generate(),
                50000
        );

        installation.uninstall(50100, Instant.now().plus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> installation.uninstall(50200, Instant.now().plus(2, ChronoUnit.HOURS)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already terminated");
    }

    @Test
    @DisplayName("Rehydration constructor validations")
    void testRehydrationConstructor() {
        InstallationId id = InstallationId.generate();
        DeviceId devId = DeviceId.generate();
        VehicleId vehId = VehicleId.generate();
        TenantId tenId = TenantId.generate();
        Instant now = Instant.now();

        DeviceInstallation rehydrated = new DeviceInstallation(
                id, devId, vehId, tenId, now, Optional.of(now.plusSeconds(3600)), 10000, Optional.of(10500)
        );

        assertThat(rehydrated.isActive()).isFalse();
        assertThat(rehydrated.getInitialOdometerKm()).isEqualTo(10000);
        assertThat(rehydrated.getFinalOdometerKm()).contains(10500);

        assertThatThrownBy(() -> new DeviceInstallation(
                id, devId, vehId, tenId, now, Optional.empty(), -5, Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }
}
