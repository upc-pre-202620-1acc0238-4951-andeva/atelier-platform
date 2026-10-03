package com.andeva.atelier.platform.billing.interfaces.rest.resources.requests;

/**
 * Request payload to cancel or schedule period-end termination of a workshop subscription.
 *
 * @author Joel Huamani Estefanero
 */
public record CancelSubscriptionRequest(
        boolean cancelImmediately,
        String cancellationReason
) {
}
