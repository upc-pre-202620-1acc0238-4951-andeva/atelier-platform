package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.domain.model.events.TenantPlanChangedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionActivatedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionCanceledEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionPastDueEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionRenewedEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests covering the TenantSubscription aggregate root lifecycle, event emission,
 * grace period calculation, cancellation semantics, and reactivation policies.
 *
 * @author Joel Huamani Estefanero
 */
class TenantSubscriptionAggregateTest {

    @Test
    @DisplayName("Should start trial subscription and enqueue TenantSubscriptionActivatedEvent")
    void shouldStartTrial() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_trial123");

        TenantSubscription sub = TenantSubscription.startTrial(tenantId, planId, customerId, 14);

        assertThat(sub.status()).isEqualTo(SubscriptionStatus.TRIALING);
        assertThat(sub.tenantId()).isEqualTo(tenantId);
        assertThat(sub.planId()).isEqualTo(planId);
        assertThat(sub.isAccessGranted()).isTrue();
        assertThat(sub.trialEndDate()).isPresent();

        assertThat(sub.domainEvents()).hasSize(1);
        Object event = sub.domainEvents().iterator().next();
        assertThat(event).isInstanceOf(TenantSubscriptionActivatedEvent.class);

        // Edge case: trial days <= 0 defaults to at least 1 day
        TenantSubscription zeroDayTrial = TenantSubscription.startTrial(tenantId, planId, customerId, 0);
        assertThat(zeroDayTrial.trialEndDate()).isPresent();
        assertThat(zeroDayTrial.currentPeriod().duration().toDays()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should activate paid subscription and enqueue TenantSubscriptionActivatedEvent")
    void shouldActivatePaidSubscription() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_paid123");
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_paid123");
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(tenantId, planId, customerId, subId, period);

        assertThat(sub.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.stripeSubscriptionId()).isEqualTo(subId);
        assertThat(sub.isAccessGranted()).isTrue();

        assertThat(sub.domainEvents()).hasSize(1);
        assertThat(sub.domainEvents().iterator().next()).isInstanceOf(TenantSubscriptionActivatedEvent.class);
    }

    @Test
    @DisplayName("Should renew period on recurring payment and emit TenantSubscriptionRenewedEvent")
    void shouldRenewPeriod() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_paid123");
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_paid123");
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(tenantId, planId, customerId, subId, period);
        sub.clearDomainEvents();

        SubscriptionPeriod nextPeriod = SubscriptionPeriod.of(period.endDate(), period.endDate().plus(Duration.ofDays(30)));
        sub.renewPeriod(nextPeriod);

        assertThat(sub.currentPeriod()).isEqualTo(nextPeriod);
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.domainEvents()).hasSize(1);
        assertThat(sub.domainEvents().iterator().next()).isInstanceOf(TenantSubscriptionRenewedEvent.class);

        // Null period rejection
        assertThatThrownBy(() -> sub.renewPeriod(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should transition to past due and verify grace period access")
    void shouldHandlePastDueAndGracePeriod() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_pd123");
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_pd123");
        Instant start = Instant.now().minus(Duration.ofDays(31));
        Instant end = Instant.now().minus(Duration.ofDays(1)); // 1 day past period end
        SubscriptionPeriod period = SubscriptionPeriod.of(start, end);

        TenantSubscription sub = TenantSubscription.activate(tenantId, planId, customerId, subId, period);
        sub.clearDomainEvents();

        sub.markPastDue();
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.PAST_DUE);
        // 1 day past end is within 5 days grace period!
        assertThat(sub.isAccessGranted()).isTrue();

        assertThat(sub.domainEvents()).hasSize(1);
        assertThat(sub.domainEvents().iterator().next()).isInstanceOf(TenantSubscriptionPastDueEvent.class);
    }

    @Test
    @DisplayName("Should deny access when past due exceeds 5 days grace period")
    void shouldDenyAccessWhenGracePeriodExpired() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_pd123");
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_pd123");
        // 6 days past period end -> grace period of 5 days has expired
        Instant start = Instant.now().minus(Duration.ofDays(36));
        Instant end = Instant.now().minus(Duration.ofDays(6));
        SubscriptionPeriod period = SubscriptionPeriod.of(start, end);

        TenantSubscription sub = TenantSubscription.activate(tenantId, planId, customerId, subId, period);
        sub.markPastDue();

        assertThat(sub.isAccessGranted()).isFalse();
    }

    @Test
    @DisplayName("Should handle markUnpaid and revoke access")
    void shouldHandleMarkUnpaid() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                StripeCustomerId.of("cus_unpaid"),
                StripeSubscriptionId.of("sub_unpaid"),
                period
        );

        sub.markUnpaid();
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.UNPAID);
        assertThat(sub.isAccessGranted()).isFalse();
    }

    @Test
    @DisplayName("Should handle cancelAtPeriodEnd flag while maintaining active access")
    void shouldHandleCancelAtPeriodEnd() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                StripeCustomerId.of("cus_cancel_pend"),
                StripeSubscriptionId.of("sub_cancel_pend"),
                period
        );

        assertThat(sub.isCancelAtPeriodEnd()).isFalse();
        sub.cancelAtPeriodEnd();

        assertThat(sub.isCancelAtPeriodEnd()).isTrue();
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.isAccessGranted()).isTrue();
    }

    @Test
    @DisplayName("Should handle immediate cancellation and prevent reactivation")
    void shouldHandleCancellation() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_cancel123");
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_cancel123");
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(tenantId, planId, customerId, subId, period);
        sub.clearDomainEvents();

        Instant cancelTimestamp = Instant.now();
        sub.cancelImmediately(cancelTimestamp);

        assertThat(sub.status()).isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(sub.canceledAt()).isPresent();
        assertThat(sub.canceledAt().get()).isEqualTo(cancelTimestamp);
        assertThat(sub.isAccessGranted()).isFalse();

        assertThat(sub.domainEvents()).hasSize(1);
        assertThat(sub.domainEvents().iterator().next()).isInstanceOf(TenantSubscriptionCanceledEvent.class);

        // Cannot reactivate canceled subscription
        assertThatThrownBy(sub::reactivate)
                .isInstanceOf(IllegalStateException.class);

        // Cancel with null timestamp falls back to Instant.now()
        TenantSubscription sub2 = TenantSubscription.activate(tenantId, planId, customerId, subId, period);
        sub2.cancelImmediately(null);
        assertThat(sub2.canceledAt()).isPresent();
    }

    @Test
    @DisplayName("Should reactivate non-canceled subscription from PAST_DUE or pending cancellation")
    void shouldReactivateSubscription() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                StripeCustomerId.of("cus_reactivate"),
                StripeSubscriptionId.of("sub_reactivate"),
                period
        );

        sub.cancelAtPeriodEnd();
        sub.markPastDue();
        assertThat(sub.isCancelAtPeriodEnd()).isTrue();
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.PAST_DUE);

        sub.reactivate();

        assertThat(sub.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.isCancelAtPeriodEnd()).isFalse();
        assertThat(sub.canceledAt()).isEmpty();
    }

    @Test
    @DisplayName("Should change plan and emit TenantPlanChangedEvent")
    void shouldChangePlan() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_change123");
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_change123");
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(tenantId, planId, customerId, subId, period);
        sub.clearDomainEvents();

        PlanId newPlanId = PlanId.generate();
        sub.changePlan(newPlanId, StripePriceId.of("price_new123"));

        assertThat(sub.planId()).isEqualTo(newPlanId);
        assertThat(sub.domainEvents()).hasSize(1);
        Object event = sub.domainEvents().iterator().next();
        assertThat(event).isInstanceOf(TenantPlanChangedEvent.class);
        TenantPlanChangedEvent planChangedEvent = (TenantPlanChangedEvent) event;
        assertThat(planChangedEvent.oldPlanId()).isEqualTo(planId);
        assertThat(planChangedEvent.newPlanId()).isEqualTo(newPlanId);

        // Null planId rejection
        assertThatThrownBy(() -> sub.changePlan(null, StripePriceId.of("price_x")))
                .isInstanceOf(NullPointerException.class);
    }
}
