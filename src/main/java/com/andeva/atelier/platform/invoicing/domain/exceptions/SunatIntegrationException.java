package com.andeva.atelier.platform.invoicing.domain.exceptions;

/**
 * Thrown when telematics transmission or fiscal validation fails with SUNAT or the authorized PSE.
 *
 * @author Joel Huamani Estefanero
 */
public class SunatIntegrationException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_SUNAT_INTEGRATION_FAILED";

    public SunatIntegrationException(String message) {
        super(ERROR_CODE, message);
    }

    public SunatIntegrationException(String message, Throwable cause) {
        super(ERROR_CODE, message + (cause != null ? ": " + cause.getMessage() : ""));
    }
}
