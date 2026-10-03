package com.andeva.atelier.platform.billing.domain.model.enums;

/**
 * Lifecycle states of a workshop tenant subscription contract.
 *
 * @author Joel Huamani Estefanero
 */
public enum SubscriptionStatus {
    TRIALING,
    ACTIVE,
    PAST_DUE,
    CANCELED,
    UNPAID,
    INCOMPLETE
}
