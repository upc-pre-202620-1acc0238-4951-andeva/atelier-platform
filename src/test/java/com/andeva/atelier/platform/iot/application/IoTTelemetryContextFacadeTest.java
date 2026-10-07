package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.acl.IoTTelemetryContextFacadeImpl;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.TelemetryLogRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.interfaces.acl.IoTTelemetryContextFacade;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.ActiveVehicleFaultsDto;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.VehicleLatestTelemetryDto;
import com.andeva.atelier.platform.iot.interfaces.acl.dto.VehicleTelemetryHealthDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link IoTTelemetryContextFacadeImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class IoTTelemetryContextFacadeTest {

    @Mock
    private TelemetryLogRepository telemetryLogRepository;
    @Mock
    private VehicleFaultRepository vehicleFaultRepository;
    @Mock
    private DeviceInstallationRepository deviceInstallationRepository;

    private IoTTelemetryContextFacade facade;

    private final VehicleId vehicleId = VehicleId.generate();
    private final TenantId tenantId = TenantId.generate();
    private final DeviceId deviceId = DeviceId.generate();

    @BeforeEach
    void setUp() {
        facade = new IoTTelemetryContextFacadeImpl(
                telemetryLogRepository,
                vehicleFaultRepository,
                deviceInstallationRepository
        );
    }

    @Test
    @DisplayName("Should retrieve and map latest vehicle telemetry")
    void shouldRetrieveAndMapLatestTelemetry() {
        Instant now = Instant.now();
        TelemetryRecord record = TelemetryRecord.of(
                now,
                vehicleId,
                tenantId,
                null,
                VehicleSpeed.of(75.0),
                EngineTemperature.of(92.5),
                EngineRpm.of(2100),
                FuelLevel.of(80.0),
                BatteryVoltage.of(14.2)
        );

        when(telemetryLogRepository.findLatestByVehicleId(vehicleId)).thenReturn(Optional.of(record));

        Optional<VehicleLatestTelemetryDto> dtoOpt = facade.getVehicleLatestTelemetry(vehicleId);
        assertThat(dtoOpt).isPresent();

        VehicleLatestTelemetryDto dto = dtoOpt.get();
        assertThat(dto.vehicleId()).isEqualTo(vehicleId.value());
        assertThat(dto.speedKmh()).isEqualTo(75.0);
        assertThat(dto.engineTempCelsius()).isEqualTo(92.5);
        assertThat(dto.engineRpm()).isEqualTo(2100);
        assertThat(dto.batteryVoltage()).isEqualTo(14.2);
    }

    @Test
    @DisplayName("Should retrieve and map active vehicle DTC faults")
    void shouldRetrieveAndMapActiveVehicleFaults() {
        VehicleFault fault = VehicleFault.detect(
                vehicleId,
                tenantId,
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Random cylinder misfire"
        );

        when(vehicleFaultRepository.findActiveByVehicleId(vehicleId)).thenReturn(List.of(fault));

        List<ActiveVehicleFaultsDto> faults = facade.getActiveFaultsForVehicle(vehicleId);
        assertThat(faults).hasSize(1);

        ActiveVehicleFaultsDto dto = faults.get(0);
        assertThat(dto.faultId()).isEqualTo(fault.getId().value());
        assertThat(dto.dtcCode()).isEqualTo("P0300");
        assertThat(dto.severity()).isEqualTo("CRITICAL");
        assertThat(dto.description()).isEqualTo("Random cylinder misfire");
    }

    @Test
    @DisplayName("Should check whether vehicle has active device installation")
    void shouldCheckActiveDeviceInstallation() {
        DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 10000);
        when(deviceInstallationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));

        assertThat(facade.hasActiveDeviceInstallation(vehicleId)).isTrue();

        VehicleId nonInstalledVehicle = VehicleId.generate();
        when(deviceInstallationRepository.findActiveByVehicleId(nonInstalledVehicle)).thenReturn(Optional.empty());

        assertThat(facade.hasActiveDeviceInstallation(nonInstalledVehicle)).isFalse();
    }

    @Test
    @DisplayName("Should calculate vehicle telemetry health scores and traffic light accurately")
    void shouldCalculateVehicleTelemetryHealthScores() {
        // Case 1: 0 faults -> Score 100, GREEN, requiresAttention = false
        when(vehicleFaultRepository.findActiveByVehicleId(vehicleId)).thenReturn(List.of());
        VehicleTelemetryHealthDto health0 = facade.getVehicleTelemetryHealth(vehicleId);
        assertThat(health0.score()).isEqualTo(100);
        assertThat(health0.trafficLight()).isEqualTo("GREEN");
        assertThat(health0.faultCount()).isEqualTo(0);
        assertThat(health0.requiresAttention()).isFalse();

        // Case 2: 1 fault -> Score 80, GREEN, requiresAttention = true
        VehicleFault fault1 = VehicleFault.detect(vehicleId, tenantId, DtcCode.of("P0420"), FaultSeverity.MEDIUM, "Catalyst");
        when(vehicleFaultRepository.findActiveByVehicleId(vehicleId)).thenReturn(List.of(fault1));
        VehicleTelemetryHealthDto health1 = facade.getVehicleTelemetryHealth(vehicleId);
        assertThat(health1.score()).isEqualTo(80);
        assertThat(health1.trafficLight()).isEqualTo("GREEN");
        assertThat(health1.faultCount()).isEqualTo(1);
        assertThat(health1.requiresAttention()).isTrue();

        // Case 3: 2 faults -> Score 60, YELLOW, requiresAttention = true
        VehicleFault fault2 = VehicleFault.detect(vehicleId, tenantId, DtcCode.of("P0117"), FaultSeverity.CRITICAL, "Temp Sensor");
        when(vehicleFaultRepository.findActiveByVehicleId(vehicleId)).thenReturn(List.of(fault1, fault2));
        VehicleTelemetryHealthDto health2 = facade.getVehicleTelemetryHealth(vehicleId);
        assertThat(health2.score()).isEqualTo(60);
        assertThat(health2.trafficLight()).isEqualTo("YELLOW");
        assertThat(health2.faultCount()).isEqualTo(2);

        // Case 4: 6 faults -> Score 0 (floored), RED, requiresAttention = true
        when(vehicleFaultRepository.findActiveByVehicleId(vehicleId)).thenReturn(List.of(fault1, fault2, fault1, fault2, fault1, fault2));
        VehicleTelemetryHealthDto health6 = facade.getVehicleTelemetryHealth(vehicleId);
        assertThat(health6.score()).isEqualTo(0);
        assertThat(health6.trafficLight()).isEqualTo("RED");
        assertThat(health6.faultCount()).isEqualTo(6);
    }
}
