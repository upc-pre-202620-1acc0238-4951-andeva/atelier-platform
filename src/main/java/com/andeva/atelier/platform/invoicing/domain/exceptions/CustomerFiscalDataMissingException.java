package com.andeva.atelier.platform.invoicing.domain.exceptions;

/**
 * Thrown when mandatory tax identification, legal name, or fiscal address data
 * is omitted during invoice emission (e.g. Factura without RUC, or Boleta over S/ 700 without identity).
 *
 * @author Joel Huamani Estefanero
 */
public class CustomerFiscalDataMissingException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_CUSTOMER_FISCAL_MISSING";

    public CustomerFiscalDataMissingException(String message) {
        super(ERROR_CODE, message);
    }
}
