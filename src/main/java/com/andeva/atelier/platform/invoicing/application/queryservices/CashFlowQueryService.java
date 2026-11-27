package com.andeva.atelier.platform.invoicing.application.queryservices;

import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
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
     * Computes the consolidated cash flow summary (total income, expenses, net cash flow,
     * labor/parts revenue, IGV collected, and optional per-branch breakdown)
     * for a given tenant (and optional branch) and date interval.
     */
    CashFlowSummary handle(GetCashFlowSummaryQuery query);

    /**
     * Retrieves the chronological list of unified cash flow movements for the entire tenant.
     */
    List<CashFlowMovement> getCashFlowMovements(TenantId tenantId, LocalDate from, LocalDate to);

    /**
     * Retrieves the chronological list of unified cash flow movements filtered by optional branch.
     */
    default List<CashFlowMovement> getCashFlowMovements(TenantId tenantId, BranchId branchId, LocalDate from, LocalDate to) {
        return getCashFlowMovements(tenantId, from, to);
    }

    /**
     * Renders and exports the official Cash Flow Statement in vector PDF format for the entire tenant.
     */
    byte[] exportCashFlowPdf(TenantId tenantId, LocalDate from, LocalDate to);

    /**
     * Renders and exports the official Cash Flow Statement in vector PDF format for a specific branch
     * or as a consolidated multi-branch corporate balance when {@code branchId} is null.
     */
    default byte[] exportCashFlowPdf(TenantId tenantId, BranchId branchId, LocalDate from, LocalDate to) {
        return exportCashFlowPdf(tenantId, from, to);
    }
}
