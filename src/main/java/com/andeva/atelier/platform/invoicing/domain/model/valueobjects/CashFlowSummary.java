package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.util.Objects;

/**
 * Analytical cash flow summary for a reporting period.
 *
 * @author Joel Huamani Estefanero
 */
public record CashFlowSummary(
        Money grossRevenue,
        Money purchaseExpenses,
        Money payrollExpenses,
        Money netCashFlow
) implements Serializable {

    public CashFlowSummary {
        Objects.requireNonNull(grossRevenue, "Gross revenue cannot be null");
        Objects.requireNonNull(purchaseExpenses, "Purchase expenses cannot be null");
        Objects.requireNonNull(payrollExpenses, "Payroll expenses cannot be null");
        Objects.requireNonNull(netCashFlow, "Net cash flow cannot be null");
    }

    public static CashFlowSummary of(
            Money grossRevenue,
            Money purchaseExpenses,
            Money payrollExpenses,
            Money netCashFlow
    ) {
        return new CashFlowSummary(grossRevenue, purchaseExpenses, payrollExpenses, netCashFlow);
    }
}
