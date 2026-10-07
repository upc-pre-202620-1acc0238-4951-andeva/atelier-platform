package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain query to retrieve the latest computed AI vehicle health diagnostic report.
 *
 * @author Joel Huamani Estefanero
 */
public record GetLatestVehicleHealthReportQuery(
        TenantId tenantId,
        VehicleId vehicleId
) implements Serializable {

    public GetLatestVehicleHealthReportQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
    }
}
