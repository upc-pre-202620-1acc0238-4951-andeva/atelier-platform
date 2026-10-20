package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.iot.domain.model.ids.HealthReportId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain query to render the institutional PDF of an issued vehicle health report.
 *
 * @author Joel Huamani Estefanero
 */
public record ExportVehicleHealthReportPdfQuery(
        TenantId tenantId,
        HealthReportId reportId
) implements Serializable {

    public ExportVehicleHealthReportPdfQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(reportId, "reportId cannot be null");
    }
}
