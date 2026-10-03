package com.andeva.atelier.platform.shared.domain.exceptions;

/**
 * Abstract runtime superclass for all domain exceptions in Atelier Platform.
 * Encapsulates a machine-readable error code for traceability and internationalization.
 *
 * @author Joel Huamani Estefanero
 */
public abstract class DomainException extends RuntimeException {
    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
