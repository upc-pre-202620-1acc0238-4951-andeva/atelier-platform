package com.andeva.atelier.platform.billing.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

/**
 * Base abstract domain exception for all business invariant breaches in the SaaS Billing & Subscriptions Bounded Context.
 *
 * @author Joel Huamani Estefanero
 */
public abstract class BillingDomainException extends DomainException {

    protected BillingDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}
