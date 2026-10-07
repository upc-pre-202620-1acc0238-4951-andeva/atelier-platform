package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.AiInferenceDiagnosticPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.VehicleHealthReportPdfGeneratorPort;
import com.andeva.atelier.platform.iot.application.internal.queryservices.*;
import com.andeva.atelier.platform.iot.domain.model.aggregates.*;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.enums.*;
import com.andeva.atelier.platform.iot.domain.model.ids.*;
import com.andeva.atelier.platform.iot.domain.model.queries.*;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.iot.domain.repositories.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests covering all IoT Query Service implementations.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class IoTQueryServicesTest {

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final DeviceId deviceId = DeviceId.generate();

    @Nested
    @DisplayName("TelemetryLogQueryService Tests")
    class TelemetryLogQueryServiceTests {
        @Mock
        private TelemetryLogRepository repository;

        @Test
        @DisplayName("Should retrieve latest telemetry and aggregated history")
        void shouldRetrieveTelemetry() {
            var service = new TelemetryLogQueryServiceImpl(repository);
            TelemetryRecord record = TelemetryRecord.of(
                    Instant.now(), vehicleId, tenantId, null,
                    VehicleSpeed.of(60.0), EngineTemperature.of(90.0),
                    EngineRpm.of(2000), FuelLevel.of(50.0), BatteryVoltage.of(13.8)
            );

            when(repository.findLatestByVehicleId(vehicleId)).thenReturn(Optional.of(record));
            when(repository.findHistoryAggregated(eq(vehicleId), any(), any(), eq("1 hour")))
                    .thenReturn(List.of(record));

            Optional<TelemetryRecord> latest = service.getLatestTelemetry(vehicleId);
            assertThat(latest).contains(record);

            List<TelemetryRecord> history = service.getAggregatedTelemetry(
                    vehicleId, Instant.now().minusSeconds(3600), Instant.now(), "1 hour"
            );
            assertThat(history).hasSize(1);
        }
    }

    @Nested
    @DisplayName("VehicleFaultQueryService Tests")
    class VehicleFaultQueryServiceTests {
        @Mock
        private VehicleFaultRepository repository;

        @Test
        @DisplayName("Should query active faults and fault history for vehicle")
        void shouldQueryFaults() {
            var service = new VehicleFaultQueryServiceImpl(repository);
            VehicleFault fault = VehicleFault.detect(
                    vehicleId, tenantId, DtcCode.of("P0300"), FaultSeverity.CRITICAL, "Cylinder misfire"
            );

            when(repository.findActiveByVehicleId(vehicleId)).thenReturn(List.of(fault));
            when(repository.findAllByVehicleId(vehicleId)).thenReturn(List.of(fault));

            assertThat(service.getActiveFaultsByVehicle(vehicleId)).containsExactly(fault);
            assertThat(service.getFaultHistoryByVehicle(vehicleId)).containsExactly(fault);
        }
    }

    @Nested
    @DisplayName("PredictiveAlertQueryService Tests")
    class PredictiveAlertQueryServiceTests {
        @Mock
        private PredictiveAlertRepository repository;

        @Test
        @DisplayName("Should query active alerts by tenant and alerts by vehicle")
        void shouldQueryAlerts() {
            var service = new PredictiveAlertQueryServiceImpl(repository);
            PredictiveAlert alert = PredictiveAlert.create(
                    vehicleId, tenantId, Optional.empty(), AlertType.ENGINE_OVERHEATING_RISK,
                    ConfidenceScore.of(90.0), "Risk"
            );

            when(repository.findAllByTenantIdAndStatus(tenantId, AlertStatus.DISPATCHED))
                    .thenReturn(List.of(alert));
            when(repository.findAllByTenantId(tenantId)).thenReturn(List.of(alert));
            when(repository.findAllByVehicleId(vehicleId)).thenReturn(List.of(alert));

            assertThat(service.getActiveAlertsByTenant(tenantId, AlertStatus.DISPATCHED)).containsExactly(alert);
            assertThat(service.getActiveAlertsByTenant(tenantId, null)).containsExactly(alert);
            assertThat(service.getAlertsByVehicle(vehicleId)).containsExactly(alert);
        }
    }

    @Nested
    @DisplayName("Obd2DeviceQueryService Tests")
    class Obd2DeviceQueryServiceTests {
        @Mock
        private Obd2DeviceRepository repository;

        @Test
        @DisplayName("Should query device by id and by tenant")
        void shouldQueryDevices() {
            var service = new Obd2DeviceQueryServiceImpl(repository);
            Obd2Device device = Obd2Device.register(
                    tenantId, DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                    ConnectionType.BLUETOOTH_BLE, "ModelX", "1.0"
            );

            when(repository.findById(deviceId)).thenReturn(Optional.of(device));
            when(repository.findAllByTenantId(tenantId)).thenReturn(List.of(device));

            assertThat(service.handle(new GetDeviceByIdQuery(deviceId))).contains(device);
            assertThat(service.getDevicesByTenant(tenantId)).containsExactly(device);
        }
    }

    @Nested
    @DisplayName("DeviceInstallationQueryService Tests")
    class DeviceInstallationQueryServiceTests {
        @Mock
        private DeviceInstallationRepository repository;

        @Test
        @DisplayName("Should query active installation and history by vehicle")
        void shouldQueryInstallations() {
            var service = new DeviceInstallationQueryServiceImpl(repository);
            DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 12000);

            when(repository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));
            when(repository.findAllHistoryByVehicleId(vehicleId)).thenReturn(List.of(installation));

            assertThat(service.handle(new GetDeviceByVehicleIdQuery(vehicleId))).contains(installation);
            assertThat(service.getInstallationHistoryByVehicle(vehicleId)).containsExactly(installation);
        }
    }

    @Nested
    @DisplayName("VehicleHealthReportQueryService Tests")
    class VehicleHealthReportQueryServiceTests {
        @Mock
        private AiInferenceDiagnosticPort aiPort;
        @Mock
        private VehicleHealthReportPdfGeneratorPort pdfPort;
        @Mock
        private CrmFleetAclPort crmPort;

        @Test
        @DisplayName("Should query latest health report and export PDF")
        void shouldQueryReportAndExportPdf() {
            var service = new VehicleHealthReportQueryServiceImpl(aiPort, pdfPort, crmPort);
            VehicleHealthReportAiDto report = new VehicleHealthReportAiDto(
                    95, "EXCELLENT", "Summary", List.of(), List.of(), List.of(), List.of()
            );

            when(aiPort.generateVehicleDiagnostic(tenantId, vehicleId, 30, true)).thenReturn(report);
            when(crmPort.getVehicleMetadata(vehicleId)).thenReturn(Optional.of(
                    new CrmFleetAclPort.VehicleMetadataDto("ABC-123", "VIN123", "Toyota", "Corolla", 2022, "John Doe")
            ));
            when(pdfPort.generateHealthReportPdf(eq(report), any())).thenReturn(new byte[]{1, 2, 3});

            Optional<VehicleHealthReportAiDto> fetched =
                    service.handle(new GetLatestVehicleHealthReportQuery(tenantId, vehicleId));
            assertThat(fetched).contains(report);

            byte[] pdfBytes = service.handle(new ExportVehicleHealthReportPdfQuery(tenantId, vehicleId, UUID.randomUUID()));
            assertThat(pdfBytes).isEqualTo(new byte[]{1, 2, 3});
        }
    }
}
