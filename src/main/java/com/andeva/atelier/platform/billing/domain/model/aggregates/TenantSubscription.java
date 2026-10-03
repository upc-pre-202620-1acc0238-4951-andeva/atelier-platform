package com.andeva.atelier.platform.billing.domain.model.aggregates;

import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.domain.model.events.TenantPlanChangedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionActivatedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionCanceledEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionPastDueEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionRenewedEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root representing the contractual SaaS subscription agreement of a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public class TenantSubscription extends AbstractDomainAggregateRoot<TenantSubscription> {

    private static final int DEFAULT_GRACE_PERIOD_DAYS = 5;

    private final SubscriptionId id;
    private final TenantId tenantId;
    private PlanId planId;
    private final StripeCustomerId stripeCustomerId;
    private StripeSubscriptionId stripeSubscriptionId;
    private SubscriptionStatus status;
    private SubscriptionPeriod currentPeriod;
    private boolean cancelAtPeriodEnd;
    private Instant canceledAt;
    private Instant trialEndDate;

    public TenantSubscription(
            SubscriptionId id,
            TenantId tenantId,
            PlanId planId,
            StripeCustomerId stripeCustomerId,
            StripeSubscriptionId stripeSubscriptionId,
            SubscriptionStatus status,
            SubscriptionPeriod currentPeriod,
            boolean cancelAtPeriodEnd,
            Instant canceledAt,
            Instant trialEndDate
    ) {
        this.id = Objects.requireNonNull(id, "SubscriptionId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.planId = Objects.requireNonNull(planId, "PlanId cannot be null");
        this.stripeCustomerId = Objects.requireNonNull(stripeCustomerId, "StripeCustomerId cannot be null");
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.status = Objects.requireNonNull(status, "SubscriptionStatus cannot be null");
        this.currentPeriod = Objects.requireNonNull(currentPeriod, "SubscriptionPeriod cannot be null");
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
        this.canceledAt = canceledAt;
        this.trialEndDate = trialEndDate;
    }

    public static TenantSubscription startTrial(
            TenantId tenantId,
            PlanId planId,
            StripeCustomerId customerId,
            int trialDays
    ) {
        SubscriptionId id = SubscriptionId.generate();
        Instant start = Instant.now();
        Instant end = start.plus(Duration.ofDays(Math.max(trialDays, 1)));
        SubscriptionPeriod period = SubscriptionPeriod.of(start, end);

        TenantSubscription subscription = new TenantSubscription(
                id,
                tenantId,
                planId,
                customerId,
                null,
                SubscriptionStatus.TRIALING,
                period,
                false,
                null,
                end
        );
        subscription.registerDomainEvent(TenantSubscriptionActivatedEvent.of(id, tenantId, planId, end));
        return subscription;
    }

    public static TenantSubscription activate(
            TenantId tenantId,
            PlanId planId,
            StripeCustomerId customerId,
            StripeSubscriptionId subscriptionId,
            SubscriptionPeriod period
    ) {
        SubscriptionId id = SubscriptionId.generate();
        TenantSubscription subscription = new TenantSubscription(
                id,
                tenantId,
                planId,
                customerId,
                subscriptionId,
                SubscriptionStatus.ACTIVE,
                period,
                false,
                null,
                null
        );
        subscription.registerDomainEvent(TenantSubscriptionActivatedEvent.of(id, tenantId, planId, period.endDate()));
        return subscription;
    }

    public void renewPeriod(SubscriptionPeriod newPeriod) {
        this.currentPeriod = Objects.requireNonNull(newPeriod, "New SubscriptionPeriod cannot be null");
        this.status = SubscriptionStatus.ACTIVE;
        registerDomainEvent(TenantSubscriptionRenewedEvent.of(this.id, this.tenantId, newPeriod.endDate()));
    }

    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
        Instant graceEnd = this.currentPeriod.endDate().plus(Duration.ofDays(DEFAULT_GRACE_PERIOD_DAYS));
        registerDomainEvent(TenantSubscriptionPastDueEvent.of(this.id, this.tenantId, graceEnd));
    }

    public void markUnpaid() {
        this.status = SubscriptionStatus.UNPAID;
    }

    public void cancelAtPeriodEnd() {
        this.cancelAtPeriodEnd = true;
    }

    public void cancelImmediately(Instant cancellationTimestamp) {
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = cancellationTimestamp != null ? cancellationTimestamp : Instant.now();
        registerDomainEvent(TenantSubscriptionCanceledEvent.of(this.id, this.tenantId, this.canceledAt));
    }

    public void reactivate() {
        if (this.status == SubscriptionStatus.CANCELED) {
            throw new IllegalStateException("A canceled subscription cannot be reactivated; a new subscription must be created");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.cancelAtPeriodEnd = false;
        this.canceledAt = null;
    }

    public void activateFromTrial(StripeSubscriptionId subscriptionId, SubscriptionPeriod period) {
        this.stripeSubscriptionId = Objects.requireNonNull(subscriptionId, "StripeSubscriptionId cannot be null");
        this.currentPeriod = Objects.requireNonNull(period, "SubscriptionPeriod cannot be null");
        this.status = SubscriptionStatus.ACTIVE;
        this.trialEndDate = null;
        registerDomainEvent(TenantSubscriptionActivatedEvent.of(this.id, this.tenantId, this.planId, period.endDate()));
    }

    public void changePlan(PlanId newPlanId, StripePriceId newPriceId) {
        Objects.requireNonNull(newPlanId, "New PlanId cannot be null");
        Objects.requireNonNull(newPriceId, "New StripePriceId cannot be null");
        PlanId oldPlanId = this.planId;
        this.planId = newPlanId;
        registerDomainEvent(TenantPlanChangedEvent.of(this.id, this.tenantId, oldPlanId, newPlanId));
    }

    public boolean isAccessGranted() {
        if (this.status == SubscriptionStatus.ACTIVE || this.status == SubscriptionStatus.TRIALING) {
            return true;
        }
        if (this.status == SubscriptionStatus.PAST_DUE) {
            Instant graceLimit = this.currentPeriod.endDate().plus(Duration.ofDays(DEFAULT_GRACE_PERIOD_DAYS));
            return Instant.now().isBefore(graceLimit);
        }
        return false;
    }

    public SubscriptionId id() {
        return id;
    }

    public SubscriptionId getId() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public PlanId planId() {
        return planId;
    }

    public PlanId getPlanId() {
        return planId;
    }

    public StripeCustomerId stripeCustomerId() {
        return stripeCustomerId;
    }

    public StripeCustomerId getStripeCustomerId() {
        return stripeCustomerId;
    }

    public StripeSubscriptionId stripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public StripeSubscriptionId getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public SubscriptionStatus status() {
        return status;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public SubscriptionPeriod currentPeriod() {
        return currentPeriod;
    }

    public SubscriptionPeriod getCurrentPeriod() {
        return currentPeriod;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public Optional<Instant> canceledAt() {
        return Optional.ofNullable(canceledAt);
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public Optional<Instant> trialEndDate() {
        return Optional.ofNullable(trialEndDate);
    }

    public Instant getTrialEndDate() {
        return trialEndDate;
    }
}
