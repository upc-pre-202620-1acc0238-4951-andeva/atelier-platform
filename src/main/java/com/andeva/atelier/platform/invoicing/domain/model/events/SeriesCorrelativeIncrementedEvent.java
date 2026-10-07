package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when the correlative of a fiscal series is incremented.
 *
 * @author Joel Huamani Estefanero
 */
public record SeriesCorrelativeIncrementedEvent(
        SeriesConfigurationId seriesId,
        TenantId tenantId,
        BranchId branchId,
        VoucherSerie serie,
        int newCorrelative,
        Instant occurredOn
) implements Serializable {

    public SeriesCorrelativeIncrementedEvent {
        Objects.requireNonNull(seriesId, "Series configuration ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    public static SeriesCorrelativeIncrementedEvent of(
            SeriesConfigurationId seriesId,
            TenantId tenantId,
            BranchId branchId,
            VoucherSerie serie,
            int newCorrelative
    ) {
        return new SeriesCorrelativeIncrementedEvent(seriesId, tenantId, branchId, serie, newCorrelative, Instant.now());
    }
}
