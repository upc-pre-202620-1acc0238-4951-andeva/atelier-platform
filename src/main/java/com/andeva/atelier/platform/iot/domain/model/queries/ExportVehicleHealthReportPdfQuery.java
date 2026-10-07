package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain query to export a generated vehicle health diagnostic report as an institutional PDF binary.
 *
 * @author Joel Huamani Estefanero
 */
public record ExportVehicleHealthReportPdfQuery(
        TenantId tenantId,
        VehicleId vehicleId,
        UUID reportId
) implements Serializable {

    public ExportVehicleHealthReportPdfQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(reportId, "reportId cannot be null");
    }
}
