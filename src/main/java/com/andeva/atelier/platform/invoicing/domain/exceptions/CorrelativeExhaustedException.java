package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;

/**
 * Thrown when the sequential correlative of a fiscal series reaches the SUNAT maximum upper limit of 99,999,999.
 *
 * @author Joel Huamani Estefanero
 */
public class CorrelativeExhaustedException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_CORRELATIVE_EXHAUSTED";

    public CorrelativeExhaustedException(VoucherSerie serie) {
        super(ERROR_CODE, "Fiscal series '" + serie.value() + "' has reached its absolute maximum upper limit of 99,999,999 correlatives.");
    }
}
