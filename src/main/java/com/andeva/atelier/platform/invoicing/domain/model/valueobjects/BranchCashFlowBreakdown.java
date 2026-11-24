package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Per-branch cash flow and fiscal revenue breakdown used in multi-branch corporate balance reports.
 */
public record BranchCashFlowBreakdown(
        UUID branchId,
        String branchName,
        Money laborRevenue,
        Money partsRevenue,
        Money igvCollected,
        Money totalRevenue
) implements Serializable {

    public BranchCashFlowBreakdown {
        Objects.requireNonNull(branchId, "Branch ID cannot be null");
        branchName = (branchName != null && !branchName.isBlank()) ? branchName : "Sede " + branchId.toString().substring(0, 8);
        Objects.requireNonNull(laborRevenue, "Labor revenue cannot be null");
        Objects.requireNonNull(partsRevenue, "Parts revenue cannot be null");
        Objects.requireNonNull(igvCollected, "IGV collected cannot be null");
        Objects.requireNonNull(totalRevenue, "Total revenue cannot be null");
    }
}
