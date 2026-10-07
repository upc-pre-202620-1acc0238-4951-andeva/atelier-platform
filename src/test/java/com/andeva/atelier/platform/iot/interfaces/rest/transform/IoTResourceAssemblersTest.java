package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.DtcTelemetryCorrelationDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.PredictiveRiskDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.RecommendedServiceActionDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertSeverity;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.BatteryVoltage;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineRpm;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineTemperature;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.FuelLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleSpeed;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.DeviceInstallationResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.HealthReportCreatedResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.Obd2DeviceResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.PredictiveAlertResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.TelemetryHistoryBucketResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.VehicleFaultResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.VehicleLatestTelemetryResponse;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for IoT REST resource assemblers.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("IoT Resource Assemblers Unit Tests")
class IoTResourceAssemblersTest {

    @Test
    @DisplayName("Obd2DeviceResourceAssembler should map aggregate to response accurately")
    void testObd2DeviceResourceAssembler() {
        Obd2DeviceResourceAssembler assembler = new Obd2DeviceResourceAssembler();
        assertThat(assembler.toResponse(null)).isNull();

        Obd2Device device = Obd2Device.register(
                TenantId.generate(),
                DeviceIdentifier.of("AA:BB:CC:DD:EE:FF"),
                ConnectionType.SIM_CELLULAR,
                "Freematics ONE+ v2",
                "v2.1.0"
        );

        Obd2DeviceResponse response = assembler.toResponse(device);
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(device.getId().value());
        assertThat(response.tenantId()).isEqualTo(device.getTenantId().value());
        assertThat(response.deviceIdentifier()).isEqualTo("AA:BB:CC:DD:EE:FF");
        assertThat(response.hardwareModel()).isEqualTo("Freematics ONE+ v2");
        assertThat(response.firmwareVersion()).isEqualTo("v2.1.0");
        assertThat(response.connectionType()).isEqualTo("SIM_CELLULAR");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("DeviceInstallationResourceAssembler should map aggregate to response accurately")
    void testDeviceInstallationResourceAssembler() {
        DeviceInstallationResourceAssembler assembler = new DeviceInstallationResourceAssembler();
        assertThat(assembler.toResponse(null)).isNull();

        DeviceInstallation installation = DeviceInstallation.install(
                DeviceId.generate(),
                VehicleId.generate(),
                TenantId.generate(),
                45000
        );

        DeviceInstallationResponse response = assembler.toResponse(installation);
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(installation.getId().value());
        assertThat(response.deviceId()).isEqualTo(installation.getDeviceId().value());
        assertThat(response.vehicleId()).isEqualTo(installation.getVehicleId().value());
        assertThat(response.tenantId()).isEqualTo(installation.getTenantId().value());
        assertThat(response.isActive()).isTrue();
        assertThat(response.initialOdometerKm()).isEqualTo(45000);
        assertThat(response.finalOdometerKm()).isNull();
    }

    @Test
    @DisplayName("TelemetryResourceAssembler should map telemetry records to response accurately")
    void testTelemetryResourceAssembler() {
        TelemetryResourceAssembler assembler = new TelemetryResourceAssembler();
        assertThat(assembler.toLatestResponse(null)).isNull();

        VehicleId vehicleId = VehicleId.generate();
        TenantId tenantId = TenantId.generate();
        Instant now = Instant.now();

        TelemetryRecord record = TelemetryRecord.of(
                now,
                vehicleId,
                tenantId,
                GeoCoordinates.of(-12.046374, -77.042793),
                VehicleSpeed.of(60.0),
                EngineTemperature.of(90.0),
                EngineRpm.of(2200),
                FuelLevel.of(70.0),
                BatteryVoltage.of(14.1)
        );

        VehicleLatestTelemetryResponse latestResponse = assembler.toLatestResponse(record);
        assertThat(latestResponse).isNotNull();
        assertThat(latestResponse.vehicleId()).isEqualTo(vehicleId.value());
        assertThat(latestResponse.timestamp()).isEqualTo(now);
        assertThat(latestResponse.latitude()).isEqualTo(-12.046374);
        assertThat(latestResponse.longitude()).isEqualTo(-77.042793);
        assertThat(latestResponse.speedKmh()).isEqualTo(60);
        assertThat(latestResponse.engineTempCelsius()).isEqualTo(90.0);
        assertThat(latestResponse.engineRpm()).isEqualTo(2200);
        assertThat(latestResponse.fuelPercentage()).isEqualTo(70.0);
        assertThat(latestResponse.batteryVoltage()).isEqualTo(14.1);

        TelemetryHistoryBucketResponse bucketResponse = assembler.toBucketResponse(
                now, vehicleId.value(), 55, 91.0, 2100, 68.0, 14.0, 10
        );
        assertThat(bucketResponse).isNotNull();
        assertThat(bucketResponse.vehicleId()).isEqualTo(vehicleId.value());
        assertThat(bucketResponse.sampleCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("VehicleFaultResourceAssembler should map aggregate to response accurately")
    void testVehicleFaultResourceAssembler() {
        VehicleFaultResourceAssembler assembler = new VehicleFaultResourceAssembler();
        assertThat(assembler.toResponse(null)).isNull();

        VehicleFault fault = VehicleFault.detect(
                VehicleId.generate(),
                TenantId.generate(),
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Random cylinder misfire"
        );

        VehicleFaultResponse response = assembler.toResponse(fault);
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(fault.getId().value());
        assertThat(response.dtcCode()).isEqualTo("P0300");
        assertThat(response.severity()).isEqualTo("CRITICAL");
        assertThat(response.isResolved()).isFalse();
        assertThat(response.description()).isEqualTo("Random cylinder misfire");
    }

    @Test
    @DisplayName("PredictiveAlertResourceAssembler should map aggregate to response accurately")
    void testPredictiveAlertResourceAssembler() {
        PredictiveAlertResourceAssembler assembler = new PredictiveAlertResourceAssembler();
        assertThat(assembler.toResponse(null)).isNull();

        PredictiveAlert alert = PredictiveAlert.create(
                VehicleId.generate(),
                TenantId.generate(),
                AlertType.BATTERY_FAILURE_RISK,
                ConfidenceScore.of(BigDecimal.valueOf(91.5)),
                "Low voltage at rest"
        );

        PredictiveAlertResponse response = assembler.toResponse(alert);
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(alert.getId().value());
        assertThat(response.alertType()).isEqualTo("BATTERY_FAILURE_RISK");
        assertThat(response.confidenceScore()).isEqualByComparingTo("91.5");
        assertThat(response.status()).isEqualTo("DISPATCHED");
    }

    @Test
    @DisplayName("VehicleHealthReportResourceAssembler should map AI DTO to response accurately")
    void testVehicleHealthReportResourceAssembler() {
        VehicleHealthReportResourceAssembler assembler = new VehicleHealthReportResourceAssembler();
        assertThat(assembler.toResponse(UUID.randomUUID(), UUID.randomUUID(), null, Instant.now())).isNull();

        UUID reportId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Instant generatedAt = Instant.now();

        VehicleHealthReportAiDto reportDto = new VehicleHealthReportAiDto(
                92,
                "EXCELLENT",
                "Vehicle operates within normal thermodynamic tolerances.",
                List.of(new com.andeva.atelier.platform.iot.domain.model.dto.ai.SubsystemEvaluationDto("Brakes", "EXCELLENT", 92, "Optimal wear")),
                List.of(new PredictiveRiskDto("BRAKES", BigDecimal.valueOf(0.15), 60, "Low wear")),
                List.of(new RecommendedServiceActionDto("SRV-BRK-01", "Brake Inspection", "LOW", BigDecimal.valueOf(50.00), "Routine check")),
                List.of(new DtcTelemetryCorrelationDto("P0000", "N/A", "No anomalies"))
        );

        HealthReportCreatedResponse response = assembler.toResponse(reportId, vehicleId, reportDto, generatedAt);
        assertThat(response).isNotNull();
        assertThat(response.reportId()).isEqualTo(reportId);
        assertThat(response.vehicleId()).isEqualTo(vehicleId);
        assertThat(response.overallHealthScore()).isEqualTo(92);
        assertThat(response.executiveSummary()).isEqualTo("Vehicle operates within normal thermodynamic tolerances.");
        assertThat(response.totalRisksDetected()).isEqualTo(1);
        assertThat(response.pdfDownloadUrl()).contains(reportId.toString());
    }
}
