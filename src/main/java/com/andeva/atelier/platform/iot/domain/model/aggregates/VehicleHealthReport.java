package com.andeva.atelier.platform.iot.domain.model.aggregates;

import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.ids.HealthReportId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root representing an issued AI-assisted vehicle health report. Once generated, the
 * diagnosis is immutable: later reads and PDF exports serve this stored version instead of
 * re-running the inference.
 *
 * @author Joel Huamani Estefanero
 */
public class VehicleHealthReport extends AbstractDomainAggregateRoot<VehicleHealthReport> {

    private final HealthReportId id;
    private final TenantId tenantId;
    private final VehicleId vehicleId;
    private final int daysAnalyzed;
    private final VehicleHealthReportAiDto diagnosis;
    private final Instant generatedAt;

    /**
     * Rehydration constructor for persistence assemblers.
     */
    public VehicleHealthReport(
            HealthReportId id,
            TenantId tenantId,
            VehicleId vehicleId,
            int daysAnalyzed,
            VehicleHealthReportAiDto diagnosis,
            Instant generatedAt
    ) {
        this.id = Objects.requireNonNull(id, "HealthReportId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        this.diagnosis = Objects.requireNonNull(diagnosis, "Diagnosis cannot be null");
        this.generatedAt = Objects.requireNonNull(generatedAt, "generatedAt cannot be null");
        if (daysAnalyzed <= 0) {
            throw new IllegalArgumentException("The analyzed telemetry window must be at least one day");
        }
        this.daysAnalyzed = daysAnalyzed;
    }

    /**
     * Domain factory recording a freshly inferred diagnosis.
     */
    public static VehicleHealthReport issue(
            TenantId tenantId,
            VehicleId vehicleId,
            int daysAnalyzed,
            VehicleHealthReportAiDto diagnosis
    ) {
        return new VehicleHealthReport(HealthReportId.generate(), tenantId, vehicleId, daysAnalyzed, diagnosis, Instant.now());
    }

    public boolean belongsTo(TenantId tenant) {
        return this.tenantId.equals(tenant);
    }

    public HealthReportId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public int getDaysAnalyzed() {
        return daysAnalyzed;
    }

    public VehicleHealthReportAiDto getDiagnosis() {
        return diagnosis;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
