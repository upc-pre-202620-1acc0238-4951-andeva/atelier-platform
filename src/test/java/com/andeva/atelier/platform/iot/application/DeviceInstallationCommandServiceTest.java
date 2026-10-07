package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.commandservices.DeviceInstallationCommandService;
import com.andeva.atelier.platform.iot.application.internal.commandservices.DeviceInstallationCommandServiceImpl;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.domain.exceptions.ActiveInstallationConflictException;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceAlreadyInstalledException;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.InstallationNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.commands.InstallDeviceOnVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UninstallDeviceFromVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.events.DeviceInstalledOnVehicleEvent;
import com.andeva.atelier.platform.iot.domain.model.events.DeviceUninstalledFromVehicleEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.Obd2DeviceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeviceInstallationCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class DeviceInstallationCommandServiceTest {

    @Mock
    private DeviceInstallationRepository deviceInstallationRepository;
    @Mock
    private Obd2DeviceRepository obd2DeviceRepository;
    @Mock
    private CrmFleetAclPort crmFleetAclPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DeviceInstallationCommandService service;

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final DeviceId deviceId = DeviceId.generate();

    @BeforeEach
    void setUp() {
        lenient().when(crmFleetAclPort.isVehicleRegistered(any())).thenReturn(true);
        service = new DeviceInstallationCommandServiceImpl(
                deviceInstallationRepository,
                obd2DeviceRepository,
                crmFleetAclPort,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should reject installation when device does not exist")
    void shouldRejectInstallationWhenDeviceNotFound() {
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                deviceId, vehicleId, tenantId, 15000
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(DeviceNotFoundException.class);

        verifyNoInteractions(deviceInstallationRepository);
    }

    @Test
    @DisplayName("Should reject installation when device is not in ACTIVE status")
    void shouldRejectInstallationWhenDeviceNotActive() {
        Obd2Device device = Obd2Device.register(
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                "Scanner-Pro",
                "1.0"
        );
        device.markBroken();
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                deviceId, vehicleId, tenantId, 15000
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be installed");
    }

    @Test
    @DisplayName("Should reject installation when device is already mounted on another vehicle")
    void shouldRejectInstallationWhenDeviceAlreadyInstalled() {
        Obd2Device device = Obd2Device.register(
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                "Scanner-Pro",
                "1.0"
        );
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        DeviceInstallation existingInstallation = DeviceInstallation.install(
                deviceId, VehicleId.generate(), tenantId, 5000
        );
        when(deviceInstallationRepository.findActiveByDeviceId(deviceId)).thenReturn(Optional.of(existingInstallation));

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                deviceId, vehicleId, tenantId, 15000
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(DeviceAlreadyInstalledException.class);
    }

    @Test
    @DisplayName("Should reject installation when vehicle already has another active device")
    void shouldRejectInstallationWhenVehicleHasActiveInstallation() {
        Obd2Device device = Obd2Device.register(
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                "Scanner-Pro",
                "1.0"
        );
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceInstallationRepository.findActiveByDeviceId(deviceId)).thenReturn(Optional.empty());

        DeviceInstallation existingVehicleInstallation = DeviceInstallation.install(
                DeviceId.generate(), vehicleId, tenantId, 5000
        );
        when(deviceInstallationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(existingVehicleInstallation));

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                deviceId, vehicleId, tenantId, 15000
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(ActiveInstallationConflictException.class);
    }

    @Test
    @DisplayName("Should successfully install device and publish DeviceInstalledOnVehicleEvent")
    void shouldSuccessfullyInstallDevice() {
        Obd2Device device = Obd2Device.register(
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                "Scanner-Pro",
                "1.0"
        );
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceInstallationRepository.findActiveByDeviceId(deviceId)).thenReturn(Optional.empty());
        when(deviceInstallationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.empty());

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                deviceId, vehicleId, tenantId, 15000
        );

        InstallationId id = service.handle(command);
        assertThat(id).isNotNull();

        ArgumentCaptor<DeviceInstallation> captor = ArgumentCaptor.forClass(DeviceInstallation.class);
        verify(deviceInstallationRepository).save(captor.capture());

        DeviceInstallation saved = captor.getValue();
        assertThat(saved.getDeviceId()).isEqualTo(deviceId);
        assertThat(saved.getVehicleId()).isEqualTo(vehicleId);
        assertThat(saved.getInitialOdometerKm()).isEqualTo(15000);
        assertThat(saved.isActive()).isTrue();

        verify(eventPublisher, atLeastOnce()).publishEvent(any(DeviceInstalledOnVehicleEvent.class));
    }

    @Test
    @DisplayName("Should uninstall device and publish DeviceUninstalledFromVehicleEvent")
    void shouldSuccessfullyUninstallDevice() {
        InstallationId installationId = InstallationId.generate();
        DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 15000);
        when(deviceInstallationRepository.findById(installationId)).thenReturn(Optional.of(installation));

        UninstallDeviceFromVehicleCommand command = new UninstallDeviceFromVehicleCommand(
                installationId, 18000
        );

        service.handle(command);

        assertThat(installation.isActive()).isFalse();
        assertThat(installation.getFinalOdometerKm()).contains(18000);

        verify(deviceInstallationRepository).save(installation);
        verify(eventPublisher, atLeastOnce()).publishEvent(any(DeviceUninstalledFromVehicleEvent.class));
    }

    @Test
    @DisplayName("Should reject uninstallation when installation not found")
    void shouldRejectUninstallationWhenNotFound() {
        InstallationId installationId = InstallationId.generate();
        when(deviceInstallationRepository.findById(installationId)).thenReturn(Optional.empty());

        UninstallDeviceFromVehicleCommand command = new UninstallDeviceFromVehicleCommand(
                installationId, 18000
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(InstallationNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject installation when device belongs to another tenant")
    void shouldRejectInstallationWhenDeviceBelongsToDifferentTenant() {
        TenantId otherTenant = TenantId.generate();
        Obd2Device device = Obd2Device.register(
                otherTenant,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                "Scanner-Pro",
                "1.0"
        );
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                deviceId, vehicleId, tenantId, 15000
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not belong to tenant");
    }

    @Test
    @DisplayName("Should reject installation when vehicle is not registered in CRM")
    void shouldRejectInstallationWhenVehicleNotRegisteredInCrm() {
        Obd2Device device = Obd2Device.register(
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                "Scanner-Pro",
                "1.0"
        );
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(crmFleetAclPort.isVehicleRegistered(vehicleId)).thenReturn(false);

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                deviceId, vehicleId, tenantId, 15000
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not registered in CRM");
    }
}
