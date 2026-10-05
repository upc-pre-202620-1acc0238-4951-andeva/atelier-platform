package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

/**
 * Base abstract domain exception for all Workshop Operations (MRO) domain invariants.
 */
public abstract class OperationsDomainException extends DomainException {

    protected OperationsDomainException(String code, String message) {
        super(code, message);
    }
}
