package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.*;
import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.enums.*;
import com.andeva.atelier.platform.iot.domain.model.ids.*;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.*;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.*;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.*;
import com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.adapters.TimescaleTelemetryRepositoryImpl;
import com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.assemblers.TimescaleTelemetryPersistenceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IoT JPA and Timescale repository adapters.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class IoTRepositoryAdaptersTest {

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final DeviceId deviceId = DeviceId.generate();

    @Mock
    private Obd2DevicePersistenceRepository obd2DevicePersistenceRepository;
    @Spy
    private Obd2DevicePersistenceAssembler obd2DeviceAssembler = new Obd2DevicePersistenceAssembler();

    @Mock
    private DeviceInstallationPersistenceRepository deviceInstallationPersistenceRepository;
    @Spy
    private DeviceInstallationPersistenceAssembler deviceInstallationAssembler = new DeviceInstallationPersistenceAssembler();

    @Mock
    private VehicleFaultPersistenceRepository vehicleFaultPersistenceRepository;
    @Spy
    private VehicleFaultPersistenceAssembler vehicleFaultAssembler = new VehicleFaultPersistenceAssembler();

    @Mock
    private PredictiveAlertPersistenceRepository predictiveAlertPersistenceRepository;
    @Spy
    private PredictiveAlertPersistenceAssembler predictiveAlertAssembler = new PredictiveAlertPersistenceAssembler();

    @Mock
    private DtcCatalogEntryPersistenceRepository dtcCatalogEntryPersistenceRepository;
    @Spy
    private DtcCatalogPersistenceAssembler dtcCatalogAssembler = new DtcCatalogPersistenceAssembler();

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Spy
    private TimescaleTelemetryPersistenceAssembler timescaleAssembler = new TimescaleTelemetryPersistenceAssembler();

    @Test
    @DisplayName("Obd2DeviceRepositoryAdapter should save, find, and check existence")
    void testObd2DeviceRepositoryAdapter() {
        var adapter = new Obd2DeviceRepositoryAdapter(obd2DevicePersistenceRepository, obd2DeviceAssembler);

        Obd2Device device = new Obd2Device(
                deviceId,
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                DeviceStatus.ACTIVE,
                "ELM327",
                "1.0"
        );
        var entity = obd2DeviceAssembler.toEntity(device);

        when(obd2DevicePersistenceRepository.save(any())).thenReturn(entity);
        when(obd2DevicePersistenceRepository.findById(deviceId.value())).thenReturn(Optional.of(entity));
        when(obd2DevicePersistenceRepository.findByDeviceIdentifier("00:1B:44:11:3A:B7")).thenReturn(Optional.of(entity));
        when(obd2DevicePersistenceRepository.findAllByTenantId(tenantId.value())).thenReturn(List.of(entity));
        when(obd2DevicePersistenceRepository.existsByDeviceIdentifier("00:1B:44:11:3A:B7")).thenReturn(true);
        when(obd2DevicePersistenceRepository.existsById(deviceId.value())).thenReturn(true);

        assertThat(adapter.save(device)).isNotNull();
        assertThat(adapter.findById(deviceId)).isPresent();
        assertThat(adapter.findByIdentifier(DeviceIdentifier.of("00:1B:44:11:3A:B7"))).isPresent();
        assertThat(adapter.findAllByTenantId(tenantId)).hasSize(1);
        assertThat(adapter.existsByIdentifier(DeviceIdentifier.of("00:1B:44:11:3A:B7"))).isTrue();
        assertThat(adapter.existsById(deviceId)).isTrue();
    }

    @Test
    @DisplayName("DeviceInstallationRepositoryAdapter should save and query active and history")
    void testDeviceInstallationRepositoryAdapter() {
        var adapter = new DeviceInstallationRepositoryAdapter(deviceInstallationPersistenceRepository, deviceInstallationAssembler);

        InstallationId id = InstallationId.generate();
        DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 12000);
        var entity = deviceInstallationAssembler.toEntity(installation);

        when(deviceInstallationPersistenceRepository.save(any())).thenReturn(entity);
        when(deviceInstallationPersistenceRepository.findById(id.value())).thenReturn(Optional.of(entity));
        when(deviceInstallationPersistenceRepository.findByVehicleIdAndUninstalledAtIsNull(vehicleId.value())).thenReturn(Optional.of(entity));
        when(deviceInstallationPersistenceRepository.findByDeviceIdAndUninstalledAtIsNull(deviceId.value())).thenReturn(Optional.of(entity));
        when(deviceInstallationPersistenceRepository.findAllByVehicleIdOrderByInstalledAtDesc(vehicleId.value())).thenReturn(List.of(entity));
        when(deviceInstallationPersistenceRepository.findAllByTenantIdAndUninstalledAtIsNull(tenantId.value())).thenReturn(List.of(entity));

        assertThat(adapter.save(installation)).isNotNull();
        assertThat(adapter.findById(id)).isPresent();
        assertThat(adapter.findActiveByVehicleId(vehicleId)).isPresent();
        assertThat(adapter.findActiveByDeviceId(deviceId)).isPresent();
        assertThat(adapter.findAllHistoryByVehicleId(vehicleId)).hasSize(1);
        assertThat(adapter.findAllActiveByTenantId(tenantId)).hasSize(1);
    }

    @Test
    @DisplayName("VehicleFaultRepositoryAdapter should save, findById, and find active faults")
    void testVehicleFaultRepositoryAdapter() {
        var adapter = new VehicleFaultRepositoryAdapter(vehicleFaultPersistenceRepository, vehicleFaultAssembler);

        FaultId faultId = FaultId.generate();
        VehicleFault fault = new VehicleFault(
                faultId,
                vehicleId,
                tenantId,
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Random Misfire",
                Instant.now(),
                false,
                Optional.empty()
        );
        var entity = vehicleFaultAssembler.toEntity(fault);

        when(vehicleFaultPersistenceRepository.save(any())).thenReturn(entity);
        when(vehicleFaultPersistenceRepository.findById(faultId.value())).thenReturn(Optional.of(entity));
        when(vehicleFaultPersistenceRepository.findAllByVehicleIdAndIsResolvedFalse(vehicleId.value())).thenReturn(List.of(entity));
        when(vehicleFaultPersistenceRepository.findAllByVehicleIdOrderByDetectedAtDesc(vehicleId.value())).thenReturn(List.of(entity));
        when(vehicleFaultPersistenceRepository.findAllByTenantId(tenantId.value())).thenReturn(List.of(entity));

        assertThat(adapter.save(fault)).isNotNull();
        assertThat(adapter.findById(faultId)).isPresent();
        assertThat(adapter.findActiveByVehicleId(vehicleId)).hasSize(1);
        assertThat(adapter.findAllByVehicleId(vehicleId)).hasSize(1);
        assertThat(adapter.findAllByTenantId(tenantId)).hasSize(1);
    }

    @Test
    @DisplayName("PredictiveAlertRepositoryAdapter should save and query alerts by vehicle and tenant")
    void testPredictiveAlertRepositoryAdapter() {
        var adapter = new PredictiveAlertRepositoryAdapter(predictiveAlertPersistenceRepository, predictiveAlertAssembler);

        AlertId alertId = AlertId.generate();
        PredictiveAlert alert = new PredictiveAlert(
                alertId,
                vehicleId,
                tenantId,
                Optional.empty(),
                AlertType.BATTERY_FAILURE_RISK,
                ConfidenceScore.of(new BigDecimal("91.20")),
                "Low battery voltage",
                AlertStatus.DISPATCHED,
                Optional.empty(),
                Instant.now()
        );
        var entity = predictiveAlertAssembler.toEntity(alert);

        when(predictiveAlertPersistenceRepository.save(any())).thenReturn(entity);
        when(predictiveAlertPersistenceRepository.findById(alertId.value())).thenReturn(Optional.of(entity));
        when(predictiveAlertPersistenceRepository.findAllByVehicleIdOrderByCreatedAtDesc(vehicleId.value())).thenReturn(List.of(entity));
        when(predictiveAlertPersistenceRepository.findAllByTenantIdAndStatus(tenantId.value(), AlertStatus.DISPATCHED)).thenReturn(List.of(entity));
        when(predictiveAlertPersistenceRepository.findAllByTenantId(tenantId.value())).thenReturn(List.of(entity));

        assertThat(adapter.save(alert)).isNotNull();
        assertThat(adapter.findById(alertId)).isPresent();
        assertThat(adapter.findAllByVehicleId(vehicleId)).hasSize(1);
        assertThat(adapter.findAllByTenantIdAndStatus(tenantId, AlertStatus.DISPATCHED)).hasSize(1);
        assertThat(adapter.findAllByTenantId(tenantId)).hasSize(1);
    }

    @Test
    @DisplayName("DtcCatalogRepositoryAdapter should find by code and category")
    void testDtcCatalogRepositoryAdapter() {
        var adapter = new DtcCatalogRepositoryAdapter(dtcCatalogEntryPersistenceRepository, dtcCatalogAssembler);

        DtcCatalogEntry entry = new DtcCatalogEntry(
                DtcCatalogId.generate(),
                DtcCode.of("P0171"),
                DtcCategory.POWERTRAIN_P,
                "System Too Lean",
                FaultSeverity.MEDIUM
        );
        var entity = dtcCatalogAssembler.toEntity(entry);

        when(dtcCatalogEntryPersistenceRepository.findByDtcCode("P0171")).thenReturn(Optional.of(entity));
        when(dtcCatalogEntryPersistenceRepository.findAllBySystemCategory(DtcCategory.POWERTRAIN_P)).thenReturn(List.of(entity));
        when(dtcCatalogEntryPersistenceRepository.existsByDtcCode("P0171")).thenReturn(true);
        when(dtcCatalogEntryPersistenceRepository.save(any())).thenReturn(entity);
        when(dtcCatalogEntryPersistenceRepository.findAll()).thenReturn(List.of(entity));

        assertThat(adapter.findByCode(DtcCode.of("P0171"))).isPresent();
        assertThat(adapter.findAllByCategory("POWERTRAIN_P")).hasSize(1);
        assertThat(adapter.findAllByCategory("INVALID_CATEGORY")).isEmpty();
        assertThat(adapter.existsByCode(DtcCode.of("P0171"))).isTrue();
        assertThat(adapter.save(entry)).isNotNull();
        assertThat(adapter.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("TimescaleTelemetryRepositoryImpl should batch insert and query latest and aggregates")
    void testTimescaleTelemetryRepository() {
        var repository = new TimescaleTelemetryRepositoryImpl(jdbcTemplate, timescaleAssembler);

        TelemetryRecord record = TelemetryRecord.of(
                Instant.now(),
                vehicleId,
                tenantId,
                null,
                VehicleSpeed.of(60.0),
                EngineTemperature.of(90.0),
                EngineRpm.of(2000),
                FuelLevel.of(50.0),
                BatteryVoltage.of(14.0)
        );

        repository.saveAllBatch(List.of(record));
        repository.executeBatchInsert(List.of(record));
        repository.saveAllBatch(List.of()); // empty test

        verify(jdbcTemplate, times(2)).batchUpdate(
                anyString(),
                anyList(),
                anyInt(),
                any(ParameterizedPreparedStatementSetter.class)
        );

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(vehicleId.value())))
                .thenReturn(List.of());

        assertThat(repository.findLatestByVehicleId(vehicleId)).isEmpty();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(vehicleId.value()), any(), any()))
                .thenReturn(List.of());

        assertThat(repository.findHistoryAggregated(vehicleId, Instant.now().minusSeconds(3600), Instant.now(), "1 hour"))
                .isEmpty();

        assertThat(repository.calculateStatisticalSummary(vehicleId, Instant.now().minusSeconds(3600), Instant.now()))
                .isEmpty();
    }
}
