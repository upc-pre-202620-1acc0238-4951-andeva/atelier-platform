package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.TelemetryIngestionCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.TelemetryLogQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.IoTDomainException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.commands.IngestTelemetryBatchCommand;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.BatteryVoltage;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineRpm;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineTemperature;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.FuelLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleSpeed;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.TelemetryBatchRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.TelemetryHistoryBucketResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.TelemetryIngestionAckResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.VehicleLatestTelemetryResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.TelemetryResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * High-performance REST controller governing batch ingestion of vehicle kinematic, thermodynamic,
 * and electrical telemetry streams into TimescaleDB, as well as live gauges and time-bucket aggregations.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/iot/telemetry")
@Tag(name = "Telemetry Ingestion", description = "High-performance endpoints for vehicle telemetry batch ingestion and analytics")
public class TelemetryIngestionController {

    private final TelemetryIngestionCommandService telemetryCommandService;
    private final TelemetryLogQueryService telemetryQueryService;
    private final TelemetryResourceAssembler telemetryAssembler;

    public TelemetryIngestionController(
            TelemetryIngestionCommandService telemetryCommandService,
            TelemetryLogQueryService telemetryQueryService,
            TelemetryResourceAssembler telemetryAssembler
    ) {
        this.telemetryCommandService = Objects.requireNonNull(telemetryCommandService, "TelemetryIngestionCommandService cannot be null");
        this.telemetryQueryService = Objects.requireNonNull(telemetryQueryService, "TelemetryLogQueryService cannot be null");
        this.telemetryAssembler = Objects.requireNonNull(telemetryAssembler, "TelemetryResourceAssembler cannot be null");
    }

    @Operation(summary = "Ingest chronological batch of vehicle telemetry readings into TimescaleDB")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Telemetry batch accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Validation failed or empty batch"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "No active device installation found for vehicle"),
            @ApiResponse(responseCode = "500", description = "TimescaleDB ingestion error")
    })
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('iot:telemetry:ingest') or hasAnyRole('ROLE_MECHANIC', 'ROLE_WORKSHOP_OWNER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<TelemetryIngestionAckResponse> ingestTelemetryBatch(
            @Valid @RequestBody TelemetryBatchRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vehicleId = VehicleId.of(request.vehicleId());

        List<TelemetryRecord> records = new ArrayList<>();
        for (var item : request.readings()) {
            GeoCoordinates coords = null;
            if (item.latitude() != null && item.longitude() != null) {
                coords = GeoCoordinates.of(item.latitude(), item.longitude());
            }

            FuelLevel fuel = item.fuelPercentage() != null ? FuelLevel.of(item.fuelPercentage()) : null;
            BatteryVoltage voltage = item.batteryVoltage() != null ? BatteryVoltage.of(item.batteryVoltage()) : null;

            TelemetryRecord record = TelemetryRecord.of(
                    item.timestamp(),
                    vehicleId,
                    tenantId,
                    coords,
                    VehicleSpeed.of(item.speedKmh()),
                    EngineTemperature.of(item.engineTempCelsius()),
                    EngineRpm.of(item.engineRpm()),
                    fuel,
                    voltage
            );
            records.add(record);
        }

        telemetryCommandService.handle(new IngestTelemetryBatchCommand(vehicleId, tenantId, records));

        TelemetryIngestionAckResponse response = new TelemetryIngestionAckResponse(
                vehicleId.value(),
                records.size(),
                false,
                null
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(summary = "Get latest telemetry reading for vehicle tachometer and indicators")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Latest telemetry reading retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "No telemetry readings found for vehicle")
    })
    @GetMapping("/vehicle/{vehicleId}/latest")
    @PreAuthorize("hasAuthority('iot:telemetry:read') or hasAnyRole('ROLE_MECHANIC', 'ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<VehicleLatestTelemetryResponse> getLatestTelemetry(
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = VehicleId.of(vehicleId);

        TelemetryRecord latest = telemetryQueryService.getLatestTelemetry(vId)
                .orElseThrow(() -> new com.andeva.atelier.platform.iot.domain.exceptions.TelemetryNotFoundException(vId));

        if (!latest.tenantId().equals(tenantId)) {
            throw new AccessDeniedException("Telemetry record does not belong to the authenticated workshop");
        }

        return ResponseEntity.ok(telemetryAssembler.toLatestResponse(latest));
    }

    @Operation(summary = "Get aggregated historical telemetry using TimescaleDB time_bucket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aggregated telemetry history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bucket interval or date range"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/vehicle/{vehicleId}/history")
    @PreAuthorize("hasAuthority('iot:telemetry:read') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC', 'ROLE_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<TelemetryHistoryBucketResponse>> getTelemetryHistory(
            @PathVariable UUID vehicleId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "1 hour") String bucket,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = VehicleId.of(vehicleId);

        Instant end = to != null ? to : Instant.now();
        Instant start = from != null ? from : end.minus(7, ChronoUnit.DAYS);

        if (start.isAfter(end)) {
            throw new IoTDomainException("Start date cannot be after end date");
        }

        List<TelemetryRecord> records = telemetryQueryService.getAggregatedTelemetry(vId, start, end, bucket);

        if (!records.isEmpty() && !records.get(0).tenantId().equals(tenantId)) {
            throw new AccessDeniedException("Telemetry history does not belong to the authenticated workshop");
        }

        List<TelemetryHistoryBucketResponse> responses = records.stream()
                .map(r -> telemetryAssembler.toBucketResponse(
                        r.timestamp(),
                        r.vehicleId().value(),
                        r.speed().kmh(),
                        r.engineTemperature().celsius(),
                        r.engineRpm().rpm(),
                        r.fuelLevel().map(FuelLevel::percentage).orElse(null),
                        r.batteryVoltage().map(BatteryVoltage::volts).orElse(null),
                        1
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
