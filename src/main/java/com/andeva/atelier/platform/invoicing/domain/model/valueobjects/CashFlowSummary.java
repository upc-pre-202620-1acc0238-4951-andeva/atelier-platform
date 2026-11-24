package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Analytical cash flow summary for a reporting period, including fiscal breakdown
 * (labor, spare parts, IGV collected) and optional per-branch corporate breakdown.
 *
 * @author Joel Huamani Estefanero
 */
public record CashFlowSummary(
        Money grossRevenue,
        Money purchaseExpenses,
        Money payrollExpenses,
        Money netCashFlow,
        Money laborRevenue,
        Money partsRevenue,
        Money igvCollected,
        List<BranchCashFlowBreakdown> branchBreakdowns
) implements Serializable {

    public CashFlowSummary {
        Objects.requireNonNull(grossRevenue, "Gross revenue cannot be null");
        Objects.requireNonNull(purchaseExpenses, "Purchase expenses cannot be null");
        Objects.requireNonNull(payrollExpenses, "Payroll expenses cannot be null");
        Objects.requireNonNull(netCashFlow, "Net cash flow cannot be null");
        Currency currency = grossRevenue.currency();
        laborRevenue = laborRevenue != null ? laborRevenue : Money.of(BigDecimal.ZERO, currency);
        partsRevenue = partsRevenue != null ? partsRevenue : Money.of(BigDecimal.ZERO, currency);
        igvCollected = igvCollected != null ? igvCollected : Money.of(BigDecimal.ZERO, currency);
        branchBreakdowns = branchBreakdowns != null ? List.copyOf(branchBreakdowns) : List.of();
    }

    public CashFlowSummary(
            Money grossRevenue,
            Money purchaseExpenses,
            Money payrollExpenses,
            Money netCashFlow
    ) {
        this(grossRevenue, purchaseExpenses, payrollExpenses, netCashFlow, null, null, null, List.of());
    }

    public static CashFlowSummary of(
            Money grossRevenue,
            Money purchaseExpenses,
            Money payrollExpenses,
            Money netCashFlow
    ) {
        return new CashFlowSummary(grossRevenue, purchaseExpenses, payrollExpenses, netCashFlow);
    }

    public static CashFlowSummary of(
            Money grossRevenue,
            Money purchaseExpenses,
            Money payrollExpenses,
            Money netCashFlow,
            Money laborRevenue,
            Money partsRevenue,
            Money igvCollected,
            List<BranchCashFlowBreakdown> branchBreakdowns
    ) {
        return new CashFlowSummary(
                grossRevenue,
                purchaseExpenses,
                payrollExpenses,
                netCashFlow,
                laborRevenue,
                partsRevenue,
                igvCollected,
                branchBreakdowns
        );
    }
}
