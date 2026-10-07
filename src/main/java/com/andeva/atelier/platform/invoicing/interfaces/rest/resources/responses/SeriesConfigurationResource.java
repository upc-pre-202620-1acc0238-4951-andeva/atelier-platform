package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Presentation resource detailing a physical branch fiscal series configuration.
 *
 * @author Joel Huamani Estefanero
 */
public record SeriesConfigurationResource(
        UUID id,
        UUID tenantId,
        UUID branchId,
        String voucherType,
        String serie,
        int currentCorrelative,
        boolean active,
        Instant createdAt
) implements Serializable {
}
