package com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.BranchCashFlowBreakdown;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.BranchCashFlowBreakdownResource;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.CashFlowMovementResource;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.CashFlowSummaryResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource Assembler transforming cash flow summaries and ledger movements into {@link CashFlowSummaryResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class CashFlowResourceAssembler {

    public CashFlowSummaryResource toResource(CashFlowSummary summary, List<CashFlowMovement> movements) {
        if (summary == null) {
            return null;
        }

        List<CashFlowMovementResource> movementResources = new ArrayList<>();
        if (movements != null) {
            for (CashFlowMovement m : movements) {
                movementResources.add(toMovementResource(m));
            }
        }

        List<BranchCashFlowBreakdownResource> branchResources = new ArrayList<>();
        if (summary.branchBreakdowns() != null) {
            for (BranchCashFlowBreakdown b : summary.branchBreakdowns()) {
                branchResources.add(new BranchCashFlowBreakdownResource(
                        b.branchId(),
                        b.branchName(),
                        b.laborRevenue().amount(),
                        b.partsRevenue().amount(),
                        b.igvCollected().amount(),
                        b.totalRevenue().amount(),
                        b.totalRevenue().currency().name()
                ));
            }
        }

        return new CashFlowSummaryResource(
                summary.grossRevenue().amount(),
                summary.purchaseExpenses().add(summary.payrollExpenses()).amount(),
                java.math.BigDecimal.ZERO,
                summary.netCashFlow().amount(),
                summary.grossRevenue().currency().name(),
                movementResources,
                summary.laborRevenue().amount(),
                summary.partsRevenue().amount(),
                summary.igvCollected().amount(),
                branchResources
        );
    }

    public CashFlowMovementResource toMovementResource(CashFlowMovement m) {
        if (m == null) {
            return null;
        }
        return new CashFlowMovementResource(
                m.transactionId(),
                m.movementDate(),
                m.type(),
                m.category(),
                m.concept(),
                m.referenceNumber(),
                m.amount(),
                m.runningBalance()
        );
    }
}
