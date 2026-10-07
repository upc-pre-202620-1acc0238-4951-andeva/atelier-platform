package com.andeva.atelier.platform.invoicing.application.queryservices;

import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;
import java.util.List;

/**
 * Query Service interface for calculating workshop cash flow summaries,
 * aggregating operational receipts and expenditures, and generating analytical statements.
 *
 * @author Joel Huamani Estefanero
 */
public interface CashFlowQueryService {

    /**
     * Computes the consolidated cash flow summary (total income, expenses, net cash flow)
     * for a given tenant and date interval.
     */
    CashFlowSummary handle(GetCashFlowSummaryQuery query);

    /**
     * Retrieves the chronological list of unified cash flow movements
     * (inflows from voucher payments, outflows from parts and labor).
     */
    List<CashFlowMovement> getCashFlowMovements(TenantId tenantId, LocalDate from, LocalDate to);

    /**
     * Renders and exports the official Cash Flow Statement in vector PDF format.
     */
    byte[] exportCashFlowPdf(TenantId tenantId, LocalDate from, LocalDate to);
}
