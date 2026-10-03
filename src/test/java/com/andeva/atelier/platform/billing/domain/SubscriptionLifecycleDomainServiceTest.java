package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.services.SubscriptionLifecycleDomainService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests covering SubscriptionLifecycleDomainService.
 * Validates 5-day grace period calculation, reactivation eligibility,
 * automatic transition policies, and boundary conditions.
 *
 * @author Joel Huamani Estefanero
 */
class SubscriptionLifecycleDomainServiceTest {

    private SubscriptionLifecycleDomainService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionLifecycleDomainService();
    }

    @Test
    @DisplayName("Should evaluate grace period validity within 5 days and reject when expired")
    void shouldEvaluateGracePeriod() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = StripeCustomerId.of("cus_life1");
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_life1");

        Instant baseTime = Instant.parse("2026-06-01T00:00:00Z");
        Instant periodEnd = baseTime.plus(Duration.ofDays(30)); // 2026-07-01T00:00:00Z
        SubscriptionPeriod period = SubscriptionPeriod.of(baseTime, periodEnd);

        TenantSubscription sub = TenantSubscription.activate(tenantId, planId, customerId, subId, period);
        sub.markPastDue();

        // 3 days after periodEnd -> within 5 days grace
        Instant withinGrace = periodEnd.plus(Duration.ofDays(3));
        assertThat(service.isGracePeriodActive(sub, withinGrace)).isTrue();

        // 4 days 23 hours after periodEnd -> within grace
        Instant nearBoundary = periodEnd.plus(Duration.ofHours(119));
        assertThat(service.isGracePeriodActive(sub, nearBoundary)).isTrue();

        // Exactly at 5 days -> grace period expired
        Instant exactBoundary = periodEnd.plus(Duration.ofDays(5));
        assertThat(service.isGracePeriodActive(sub, exactBoundary)).isFalse();

        // 6 days after periodEnd -> expired grace
        Instant expiredGrace = periodEnd.plus(Duration.ofDays(6));
        assertThat(service.isGracePeriodActive(sub, expiredGrace)).isFalse();

        // Null subscription returns false
        assertThat(service.isGracePeriodActive(null, withinGrace)).isFalse();
    }

    @Test
    @DisplayName("Should return false for grace period if subscription is not PAST_DUE")
    void shouldReturnFalseForNonPastDueSubscriptions() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription activeSub = TenantSubscription.activate(
                tenantId,
                planId,
                StripeCustomerId.of("cus_act_grace"),
                StripeSubscriptionId.of("sub_act_grace"),
                period
        );
        assertThat(service.isGracePeriodActive(activeSub, Instant.now())).isFalse();

        TenantSubscription trialSub = TenantSubscription.startTrial(
                tenantId,
                planId,
                StripeCustomerId.of("cus_trial_grace"),
                14
        );
        assertThat(service.isGracePeriodActive(trialSub, Instant.now())).isFalse();
    }

    @Test
    @DisplayName("Should evaluate reactivation eligibility across all subscription statuses")
    void shouldEvaluateReactivation() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                StripeCustomerId.of("cus_react"),
                StripeSubscriptionId.of("sub_react"),
                period
        );

        // ACTIVE is not eligible (already active)
        assertThat(service.canReactivate(sub)).isFalse();

        // PAST_DUE is eligible
        sub.markPastDue();
        assertThat(service.canReactivate(sub)).isTrue();

        // UNPAID is eligible
        sub.markUnpaid();
        assertThat(service.canReactivate(sub)).isTrue();

        // CANCELED is NOT eligible
        sub.cancelImmediately(Instant.now());
        assertThat(service.canReactivate(sub)).isFalse();

        // Null subscription is NOT eligible
        assertThat(service.canReactivate(null)).isFalse();
    }

    @Test
    @DisplayName("Should transition PAST_DUE to UNPAID when grace period expires")
    void shouldEvaluateExpirationPolicy() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        Instant periodStart = Instant.now().minus(Duration.ofDays(40));
        Instant periodEnd = Instant.now().minus(Duration.ofDays(10)); // 10 days ago (grace period was 5 days)
        SubscriptionPeriod period = SubscriptionPeriod.of(periodStart, periodEnd);

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                StripeCustomerId.of("cus_exp"),
                StripeSubscriptionId.of("sub_exp"),
                period
        );
        sub.markPastDue();

        service.evaluateExpirationPolicy(sub, Instant.now());
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.UNPAID);
    }

    @Test
    @DisplayName("Should maintain PAST_DUE status when grace period is still active")
    void shouldMaintainPastDueWhenGraceActive() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        Instant periodStart = Instant.now().minus(Duration.ofDays(30));
        Instant periodEnd = Instant.now().minus(Duration.ofDays(2)); // 2 days ago (within 5 days grace)
        SubscriptionPeriod period = SubscriptionPeriod.of(periodStart, periodEnd);

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                StripeCustomerId.of("cus_grace_act"),
                StripeSubscriptionId.of("sub_grace_act"),
                period
        );
        sub.markPastDue();

        service.evaluateExpirationPolicy(sub, Instant.now());
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.PAST_DUE);

        // Null subscription handling is safe
        assertThatCode(() -> service.evaluateExpirationPolicy(null, Instant.now()))
                .doesNotThrowAnyException();
    }
}
