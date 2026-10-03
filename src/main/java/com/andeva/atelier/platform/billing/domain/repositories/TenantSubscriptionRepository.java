package com.andeva.atelier.platform.billing.domain.repositories;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository contract for managing workshop tenant subscription contracts.
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantSubscriptionRepository {

    /**
     * Saves or updates a tenant subscription in persistence.
     *
     * @param subscription TenantSubscription aggregate to persist
     * @return persisted TenantSubscription instance
     */
    TenantSubscription save(TenantSubscription subscription);

    /**
     * Finds a subscription by its internal unique identifier.
     *
     * @param id SubscriptionId
     * @return Optional containing the subscription if found
     */
    Optional<TenantSubscription> findById(SubscriptionId id);

    /**
     * Finds the current subscription associated with a workshop tenant.
     *
     * @param tenantId TenantId
     * @return Optional containing the subscription if found
     */
    Optional<TenantSubscription> findByTenantId(TenantId tenantId);

    /**
     * Finds a subscription by its external Stripe subscription identifier.
     *
     * @param stripeSubId StripeSubscriptionId
     * @return Optional containing the subscription if found
     */
    Optional<TenantSubscription> findByStripeSubscriptionId(StripeSubscriptionId stripeSubId);

    /**
     * Checks whether an active or trialing subscription exists for the tenant.
     *
     * @param tenantId TenantId
     * @return true if an active or trialing subscription exists
     */
    boolean existsActiveByTenantId(TenantId tenantId);

    /**
     * Finds all subscriptions matching a specific lifecycle status.
     *
     * @param status SubscriptionStatus
     * @return list of matching TenantSubscriptions
     */
    List<TenantSubscription> findAllByStatus(SubscriptionStatus status);
}
