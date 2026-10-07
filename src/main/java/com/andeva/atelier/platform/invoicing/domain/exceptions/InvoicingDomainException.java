package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

/**
 * Root domain exception for all business invariant and fiscal rule violations
 * in the Invoicing & Compliance Bounded Context.
 *
 * @author Joel Huamani Estefanero
 */
public class InvoicingDomainException extends DomainException {

    public static final String ERROR_CODE = "ERR_INVOICING_DOMAIN_ROOT";

    public InvoicingDomainException(String message) {
        super(ERROR_CODE, message);
    }

    public InvoicingDomainException(String errorCode, String message) {
        super(errorCode != null ? errorCode : ERROR_CODE, message);
    }
}
