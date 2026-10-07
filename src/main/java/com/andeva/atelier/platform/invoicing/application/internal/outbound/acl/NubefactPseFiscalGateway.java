package com.andeva.atelier.platform.invoicing.application.internal.outbound.acl;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;

/**
 * Outbound ACL Port abstracting telematics communication, digital signature,
 * and OASIS UBL 2.1 XML serialization with the authorized Peruvian PSE/OSE Nubefact.
 *
 * @author Joel Huamani Estefanero
 */
public interface NubefactPseFiscalGateway {

    /**
     * Dispatches an electronic invoice (Factura) or receipt (Boleta) to Nubefact/SUNAT.
     */
    NubefactDispatchResult dispatchVoucher(ElectronicVoucher voucher);

    /**
     * Dispatches a credit note (Nota de Crédito) linked to a prior voucher.
     */
    NubefactDispatchResult dispatchCreditNote(ElectronicVoucher creditNote);

    /**
     * Communicates formal cancellation (baja) of an electronic voucher to SUNAT.
     */
    NubefactVoidResult voidVoucher(ElectronicVoucher voucher, String voidReason);

    record NubefactDispatchResult(
            boolean isAccepted,
            String responseCode,
            String description,
            String digitalSignatureHash,
            DigitalReceiptUrls urls
    ) {}

    record NubefactVoidResult(
            boolean isAccepted,
            String responseCode,
            String description,
            String ticketNumber
    ) {}
}
