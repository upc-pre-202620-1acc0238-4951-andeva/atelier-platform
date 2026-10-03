package com.andeva.atelier.platform.billing.domain.services;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Domain Service governing subscription contract lifecycle transitions, grace periods,
 * suspension enforcement, and reactivation eligibility policies.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class SubscriptionLifecycleDomainService {

    private static final int DEFAULT_GRACE_PERIOD_DAYS = 5;

    /**
     * Determines whether a PAST_DUE subscription is still within its statutory 5-day grace period.
     *
     * @param subscription     TenantSubscription to inspect
     * @param currentTimestamp current point in time
     * @return true if within grace period, false otherwise
     */
    public boolean isGracePeriodActive(TenantSubscription subscription, Instant currentTimestamp) {
        if (subscription == null || subscription.status() != SubscriptionStatus.PAST_DUE) {
            return false;
        }
        Instant periodEnd = subscription.currentPeriod().endDate();
        Instant graceLimit = periodEnd.plus(Duration.ofDays(DEFAULT_GRACE_PERIOD_DAYS));
        return currentTimestamp.isBefore(graceLimit);
    }

    /**
     * Determines whether a subscription qualifies for immediate reactivation without re-contracting.
     *
     * @param subscription TenantSubscription to evaluate
     * @return true if eligible for reactivation
     */
    public boolean canReactivate(TenantSubscription subscription) {
        if (subscription == null) {
            return false;
        }
        return subscription.status() == SubscriptionStatus.PAST_DUE
                || subscription.status() == SubscriptionStatus.UNPAID;
    }

    /**
     * Evaluates expiration and automatic suspension policies: if a subscription is in PAST_DUE
     * and the grace period has expired, it is transitioned to UNPAID.
     *
     * @param subscription     TenantSubscription to evaluate
     * @param currentTimestamp current point in time
     */
    public void evaluateExpirationPolicy(TenantSubscription subscription, Instant currentTimestamp) {
        if (subscription != null
                && subscription.status() == SubscriptionStatus.PAST_DUE
                && !isGracePeriodActive(subscription, currentTimestamp)) {
            subscription.markUnpaid();
        }
    }
}
