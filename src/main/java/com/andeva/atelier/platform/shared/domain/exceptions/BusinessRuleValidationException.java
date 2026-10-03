package com.andeva.atelier.platform.shared.domain.exceptions;

/**
 * Thrown when a domain business invariant or validation rule is violated.
 *
 * @author Joel Huamani Estefanero
 */
public class BusinessRuleValidationException extends DomainException {
    public BusinessRuleValidationException(String errorCode, String message) {
        super(errorCode, message);
    }
}
