package com.andeva.atelier.platform.iot.domain.model.aggregates;

import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.events.VehicleFaultDetectedEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root representing an electronic Diagnostic Trouble Code (DTC) fault
 * emitted by a vehicle ECU and tracked through its resolution cycle.
 *
 * @author Joel Huamani Estefanero
 */
public class VehicleFault extends AbstractDomainAggregateRoot<VehicleFault> {

    private final FaultId id;
    private final VehicleId vehicleId;
    private final TenantId tenantId;
    private final DtcCode dtcCode;
    private final FaultSeverity severity;
    private final String description;
    private final Instant detectedAt;
    private boolean isResolved;
    private Instant resolvedAt;

    /**
     * Rehydration constructor for persistence assemblers.
     */
    public VehicleFault(
            FaultId id,
            VehicleId vehicleId,
            TenantId tenantId,
            DtcCode dtcCode,
            FaultSeverity severity,
            String description,
            Instant detectedAt,
            boolean isResolved,
            Optional<Instant> resolvedAt
    ) {
        this.id = Objects.requireNonNull(id, "FaultId cannot be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.dtcCode = Objects.requireNonNull(dtcCode, "DtcCode cannot be null");
        this.severity = Objects.requireNonNull(severity, "FaultSeverity cannot be null");
        this.description = description != null ? description : "Diagnostic Trouble Code " + dtcCode.value();
        this.detectedAt = Objects.requireNonNull(detectedAt, "detectedAt cannot be null");
        this.isResolved = isResolved;
        this.resolvedAt = resolvedAt != null ? resolvedAt.orElse(null) : null;
        if (this.resolvedAt != null && this.resolvedAt.isBefore(this.detectedAt)) {
            throw new IllegalArgumentException("Resolution timestamp cannot be prior to detection timestamp");
        }
    }

    /**
     * Domain factory method recording a newly detected vehicle diagnostic trouble code.
     */
    public static VehicleFault detect(
            VehicleId vehicleId,
            TenantId tenantId,
            DtcCode dtcCode,
            FaultSeverity severity,
            String description
    ) {
        FaultId faultId = FaultId.generate();
        VehicleFault fault = new VehicleFault(
                faultId,
                vehicleId,
                tenantId,
                dtcCode,
                severity,
                description,
                Instant.now(),
                false,
                Optional.empty()
        );
        fault.registerDomainEvent(VehicleFaultDetectedEvent.of(faultId, vehicleId, tenantId, dtcCode, severity));
        return fault;
    }

    public void markResolved(Instant resolvedTimestamp) {
        Instant timestamp = resolvedTimestamp != null ? resolvedTimestamp : Instant.now();
        if (timestamp.isBefore(this.detectedAt)) {
            throw new IllegalArgumentException("Resolution timestamp cannot be prior to detection timestamp");
        }
        this.isResolved = true;
        this.resolvedAt = timestamp;
    }

    public void markResolved() {
        markResolved(Instant.now());
    }

    public void resolve() {
        markResolved();
    }

    public FaultId getId() {
        return id;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public DtcCode getDtcCode() {
        return dtcCode;
    }

    public FaultSeverity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public Optional<Instant> getResolvedAt() {
        return Optional.ofNullable(resolvedAt);
    }
}
