package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a new fiscal series configuration is established for a branch.
 *
 * @author Joel Huamani Estefanero
 */
public record SeriesConfigurationCreatedEvent(
        SeriesConfigurationId seriesId,
        TenantId tenantId,
        BranchId branchId,
        VoucherType type,
        VoucherSerie serie,
        int initialCorrelative,
        Instant occurredOn
) implements Serializable {

    public SeriesConfigurationCreatedEvent {
        Objects.requireNonNull(seriesId, "Series configuration ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        Objects.requireNonNull(type, "Voucher type cannot be null");
        Objects.requireNonNull(serie, "Voucher series cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    public static SeriesConfigurationCreatedEvent of(
            SeriesConfigurationId seriesId,
            TenantId tenantId,
            BranchId branchId,
            VoucherType type,
            VoucherSerie serie,
            int initialCorrelative
    ) {
        return new SeriesConfigurationCreatedEvent(seriesId, tenantId, branchId, type, serie, initialCorrelative, Instant.now());
    }
}
