package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

/**
 * Base abstract domain exception for all business invariant breaches in the IAM and Tenancy Bounded Context.
 *
 * @author Joel Huamani Estefanero
 */
public abstract class IamDomainException extends DomainException {

    protected IamDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}
