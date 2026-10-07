package com.andeva.atelier.platform.invoicing.domain.model.queries;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Query requesting all vouchers for a tenant within a date range and optional type filter.
 *
 * @author Joel Huamani Estefanero
 */
public record GetVouchersByTenantQuery(
        TenantId tenantId,
        Optional<VoucherType> type,
        LocalDate from,
        LocalDate to
) implements Serializable {

    public GetVouchersByTenantQuery {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(type, "Type optional cannot be null");
        Objects.requireNonNull(from, "From date cannot be null");
        Objects.requireNonNull(to, "To date cannot be null");
    }

    public static GetVouchersByTenantQuery of(TenantId tenantId, VoucherType type, LocalDate from, LocalDate to) {
        return new GetVouchersByTenantQuery(tenantId, Optional.ofNullable(type), from, to);
    }
}
