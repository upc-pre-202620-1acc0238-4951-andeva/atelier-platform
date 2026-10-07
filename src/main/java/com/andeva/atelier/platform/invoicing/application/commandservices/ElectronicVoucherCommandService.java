package com.andeva.atelier.platform.invoicing.application.commandservices;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueCreditNoteCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.ProcessSunatResponseCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoidElectronicVoucherCommand;

/**
 * Command Service interface orchestrating transactional issuance, credit notes,
 * voiding, and SUNAT responses for electronic vouchers.
 *
 * @author Joel Huamani Estefanero
 */
public interface ElectronicVoucherCommandService {

    /**
     * Issues an electronic invoice (Factura) or receipt (Boleta).
     */
    ElectronicVoucher handle(IssueElectronicVoucherCommand command);

    /**
     * Issues an electronic credit note referencing a previous voucher.
     */
    ElectronicVoucher handle(IssueCreditNoteCommand command);

    /**
     * Communicates formal cancellation/voiding of a voucher before SUNAT.
     */
    ElectronicVoucher handle(VoidElectronicVoucherCommand command);

    /**
     * Processes asynchronous SUNAT CDR response from Nubefact or webhook.
     */
    ElectronicVoucher handle(ProcessSunatResponseCommand command);
}
