package com.andeva.atelier.platform.billing.domain.exceptions;

/**
 * Thrown when an operational action breaches the limits or feature gates of the tenant's subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
public class QuotaExceededException extends BillingDomainException {

    public QuotaExceededException(String message) {
        super("QUOTA_EXCEEDED", message);
    }
}
