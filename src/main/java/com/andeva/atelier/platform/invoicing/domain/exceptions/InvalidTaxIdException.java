package com.andeva.atelier.platform.invoicing.domain.exceptions;

/**
 * Thrown when a tax identification number (RUC/DNI) fails official SUNAT verification algorithms.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidTaxIdException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_INVALID_TAX_ID";

    public InvalidTaxIdException(String taxId) {
        super(ERROR_CODE, "Tax identification number '" + taxId + "' is mathematically invalid under SUNAT Modulo 11 check algorithm.");
    }

    public InvalidTaxIdException(String taxId, String details) {
        super(ERROR_CODE, "Tax identification number '" + taxId + "' is invalid: " + details);
    }
}
