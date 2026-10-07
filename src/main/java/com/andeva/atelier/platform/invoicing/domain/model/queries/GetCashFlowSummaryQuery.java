package com.andeva.atelier.platform.invoicing.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Query requesting the analytical cash flow summary for a branch or entire tenant over a specified period.
 *
 * @author Joel Huamani Estefanero
 */
public record GetCashFlowSummaryQuery(
        TenantId tenantId,
        BranchId branchId,
        LocalDate from,
        LocalDate to
) implements Serializable {

    public GetCashFlowSummaryQuery {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(from, "From date cannot be null");
        Objects.requireNonNull(to, "To date cannot be null");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
    }

    public GetCashFlowSummaryQuery(TenantId tenantId, LocalDate from, LocalDate to) {
        this(tenantId, null, from, to);
    }

    public Optional<BranchId> optionalBranchId() {
        return Optional.ofNullable(branchId);
    }
}
