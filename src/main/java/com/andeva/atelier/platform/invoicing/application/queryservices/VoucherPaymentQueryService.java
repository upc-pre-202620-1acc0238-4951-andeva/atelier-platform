package com.andeva.atelier.platform.invoicing.application.queryservices;

import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherPaymentsQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

import java.time.LocalDate;
import java.util.List;

/**
 * Query Service interface for retrieving payments and cash ledger entries.
 *
 * @author Joel Huamani Estefanero
 */
public interface VoucherPaymentQueryService {

    /**
     * Retrieves all payment transactions recorded against a specific electronic voucher.
     */
    List<VoucherPayment> handle(GetVoucherPaymentsQuery query);

    /**
     * Retrieves all daily payment receipts recorded at a specific branch for cash reconciliation.
     */
    List<VoucherPayment> getDailyPaymentsByBranch(BranchId branchId, LocalDate date);
}
