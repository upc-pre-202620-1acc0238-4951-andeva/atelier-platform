package com.andeva.atelier.platform.invoicing.application.internal.outbound.acl;

import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;

import java.util.List;
import java.util.UUID;

/**
 * Outbound ACL Port for rendering official corporate Cash Flow Statements
 * in vector PDF format styled as a corporate bank account statement.
 *
 * @author Joel Huamani Estefanero
 */
public interface CashFlowPdfGeneratorPort {

    /**
     * Generates vector PDF document of cash flow movements and executive financial summary.
     */
    byte[] generateCashFlowPdf(UUID tenantId, CashFlowSummary summary, List<CashFlowMovement> movements);
}
