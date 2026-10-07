package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.commandservices.Obd2DeviceCommandService;
import com.andeva.atelier.platform.iot.application.internal.commandservices.Obd2DeviceCommandServiceImpl;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.IoTSubscriptionQuotaPort;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceNotFoundException;
import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDeviceIdentifierException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterObd2DeviceCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UpdateDeviceStatusCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.events.Obd2DeviceRegisteredEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.domain.repositories.Obd2DeviceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Obd2DeviceCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class Obd2DeviceCommandServiceTest {

    @Mock
    private Obd2DeviceRepository obd2DeviceRepository;
    @Mock
    private IoTSubscriptionQuotaPort subscriptionQuotaPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Obd2DeviceCommandService service;

    private final TenantId tenantId = TenantId.generate();
    private final DeviceIdentifier validMac = DeviceIdentifier.of("00:1B:44:11:3A:B7");

    @BeforeEach
    void setUp() {
        service = new Obd2DeviceCommandServiceImpl(
                obd2DeviceRepository,
                subscriptionQuotaPort,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should successfully register device after validating quota and identifier uniqueness")
    void shouldSuccessfullyRegisterDevice() {
        when(obd2DeviceRepository.findAllByTenantId(tenantId)).thenReturn(List.of());
        when(obd2DeviceRepository.existsByIdentifier(validMac)).thenReturn(false);

        RegisterObd2DeviceCommand command = new RegisterObd2DeviceCommand(
                tenantId,
                validMac,
                ConnectionType.BLUETOOTH_BLE,
                "ELM327-v2.2",
                "2.2.0"
        );

        DeviceId deviceId = service.handle(command);
        assertThat(deviceId).isNotNull();

        verify(subscriptionQuotaPort).validateObd2DeviceRegistrationAllowed(tenantId, 0);

        ArgumentCaptor<Obd2Device> captor = ArgumentCaptor.forClass(Obd2Device.class);
        verify(obd2DeviceRepository).save(captor.capture());

        Obd2Device saved = captor.getValue();
        assertThat(saved.getTenantId()).isEqualTo(tenantId);
        assertThat(saved.getDeviceIdentifier()).isEqualTo(validMac);
        assertThat(saved.getStatus()).isEqualTo(DeviceStatus.ACTIVE);

        verify(eventPublisher, atLeastOnce()).publishEvent(any(Obd2DeviceRegisteredEvent.class));
    }

    @Test
    @DisplayName("Should reject registration when device identifier already exists in inventory")
    void shouldRejectRegistrationWhenIdentifierAlreadyExists() {
        when(obd2DeviceRepository.findAllByTenantId(tenantId)).thenReturn(List.of());
        when(obd2DeviceRepository.existsByIdentifier(validMac)).thenReturn(true);

        RegisterObd2DeviceCommand command = new RegisterObd2DeviceCommand(
                tenantId,
                validMac,
                ConnectionType.BLUETOOTH_BLE,
                "ELM327-v2.2",
                "2.2.0"
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(InvalidDeviceIdentifierException.class)
                .hasMessageContaining("already registered");

        verify(obd2DeviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update device operational status to LOST, BROKEN, INACTIVE, ACTIVE")
    void shouldUpdateDeviceStatus() {
        DeviceId deviceId = DeviceId.generate();
        Obd2Device device = Obd2Device.register(
                tenantId, validMac, ConnectionType.BLUETOOTH_BLE, "ELM327", "1.0"
        );
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        service.handle(new UpdateDeviceStatusCommand(deviceId, DeviceStatus.LOST));
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.LOST);

        service.handle(new UpdateDeviceStatusCommand(deviceId, DeviceStatus.BROKEN));
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.BROKEN);

        service.handle(new UpdateDeviceStatusCommand(deviceId, DeviceStatus.INACTIVE));
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE);

        service.handle(new UpdateDeviceStatusCommand(deviceId, DeviceStatus.ACTIVE));
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.ACTIVE);

        verify(obd2DeviceRepository, times(4)).save(device);
    }

    @Test
    @DisplayName("Should throw DeviceNotFoundException when updating non-existent device")
    void shouldThrowWhenUpdatingNonExistentDevice() {
        DeviceId deviceId = DeviceId.generate();
        when(obd2DeviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new UpdateDeviceStatusCommand(deviceId, DeviceStatus.LOST)))
                .isInstanceOf(DeviceNotFoundException.class);
    }
}
