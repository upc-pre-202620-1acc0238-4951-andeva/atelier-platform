package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain query to render the consolidated health report of a customer's fleet.
 */
public record ExportFleetHealthReportPdfQuery(
        TenantId tenantId,
        UUID customerId
) implements Serializable {

    public ExportFleetHealthReportPdfQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(customerId, "customerId cannot be null");
    }
}
