package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.events.SubscriptionPlanCreatedEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanFeatureId;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for SubscriptionPlan aggregate root.
 * Covers plan creation with/without features, domain events, detail modifications,
 * feature lifecycle management, and activation state transitions.
 *
 * @author Joel Huamani Estefanero
 */
class SubscriptionPlanAggregateTest {

    @Test
    @DisplayName("Should create subscription plan without features and enqueue SubscriptionPlanCreatedEvent")
    void shouldCreateSubscriptionPlanWithoutFeatures() {
        StripePriceId priceId = StripePriceId.of("price_pro_monthly");
        PlanPricing pricing = PlanPricing.of(Money.soles(new BigDecimal("299.00")), BillingCycle.MONTHLY);
        TenantQuotaLimits quotas = TenantQuotaLimits.proPreset();

        SubscriptionPlan plan = SubscriptionPlan.create(priceId, "Pro Workshop", PlanTier.PRO, pricing, quotas);

        assertThat(plan.id()).isNotNull();
        assertThat(plan.stripePriceId()).isEqualTo(priceId);
        assertThat(plan.name()).isEqualTo("Pro Workshop");
        assertThat(plan.tier()).isEqualTo(PlanTier.PRO);
        assertThat(plan.pricing()).isEqualTo(pricing);
        assertThat(plan.quotaLimits()).isEqualTo(quotas);
        assertThat(plan.features()).isEmpty();
        assertThat(plan.isActive()).isTrue();

        Collection<Object> events = plan.domainEvents();
        assertThat(events).hasSize(1);
        Object event = events.iterator().next();
        assertThat(event).isInstanceOf(SubscriptionPlanCreatedEvent.class);
        SubscriptionPlanCreatedEvent createdEvent = (SubscriptionPlanCreatedEvent) event;
        assertThat(createdEvent.planId()).isEqualTo(plan.id());
        assertThat(createdEvent.name()).isEqualTo("Pro Workshop");
        assertThat(createdEvent.tier()).isEqualTo(PlanTier.PRO);
        assertThat(createdEvent.price()).isEqualTo(pricing.price());
    }

    @Test
    @DisplayName("Should create subscription plan with initial features list")
    void shouldCreateSubscriptionPlanWithFeatures() {
        StripePriceId priceId = StripePriceId.of("price_max_monthly");
        PlanPricing pricing = PlanPricing.of(Money.soles(new BigDecimal("599.00")), BillingCycle.MONTHLY);
        TenantQuotaLimits quotas = TenantQuotaLimits.maxPreset();

        PlanFeature f1 = PlanFeature.of(PlanFeatureId.generate(), PlanId.generate(), "FEATURE_ERP_SYNC", "ERP Sync", true);
        PlanFeature f2 = PlanFeature.of(PlanFeatureId.generate(), PlanId.generate(), "FEATURE_AI_DIAG", "AI Diagnostics", true);

        SubscriptionPlan plan = SubscriptionPlan.create(priceId, "Max Enterprise", PlanTier.MAX, pricing, quotas, List.of(f1, f2));

        assertThat(plan.features()).hasSize(2);
        assertThat(plan.hasFeature("FEATURE_ERP_SYNC")).isTrue();
        assertThat(plan.hasFeature("FEATURE_AI_DIAG")).isTrue();
        assertThat(plan.domainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should validate required fields on subscription plan creation")
    void shouldValidateCreationInvariants() {
        StripePriceId priceId = StripePriceId.of("price_go_monthly");
        PlanPricing pricing = PlanPricing.of(Money.soles(new BigDecimal("99.00")), BillingCycle.MONTHLY);
        TenantQuotaLimits quotas = TenantQuotaLimits.goPreset();

        assertThatThrownBy(() -> SubscriptionPlan.create(null, "Go", PlanTier.GO, pricing, quotas))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("StripePriceId");

        assertThatThrownBy(() -> SubscriptionPlan.create(priceId, null, PlanTier.GO, pricing, quotas))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");

        assertThatThrownBy(() -> SubscriptionPlan.create(priceId, "   ", PlanTier.GO, pricing, quotas))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");

        assertThatThrownBy(() -> SubscriptionPlan.create(priceId, "Go", null, pricing, quotas))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("PlanTier");

        assertThatThrownBy(() -> SubscriptionPlan.create(priceId, "Go", PlanTier.GO, null, quotas))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("PlanPricing");

        assertThatThrownBy(() -> SubscriptionPlan.create(priceId, "Go", PlanTier.GO, pricing, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TenantQuotaLimits");
    }

    @Test
    @DisplayName("Should update subscription plan details and reject invalid values")
    void shouldUpdateDetails() {
        SubscriptionPlan plan = SubscriptionPlan.create(
                StripePriceId.of("price_go_monthly"),
                "Go Basic",
                PlanTier.GO,
                PlanPricing.of(Money.soles(new BigDecimal("99.00")), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPreset()
        );

        PlanPricing newPricing = PlanPricing.of(Money.soles(new BigDecimal("119.00")), BillingCycle.MONTHLY);
        TenantQuotaLimits newQuotas = TenantQuotaLimits.proPreset();
        plan.updateDetails("Go Plus", newPricing, newQuotas);

        assertThat(plan.name()).isEqualTo("Go Plus");
        assertThat(plan.pricing()).isEqualTo(newPricing);
        assertThat(plan.quotaLimits()).isEqualTo(newQuotas);

        // Blank name rejection
        assertThatThrownBy(() -> plan.updateDetails("", newPricing, newQuotas))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plan.updateDetails("   ", newPricing, newQuotas))
                .isInstanceOf(IllegalArgumentException.class);

        // Null checks
        assertThatThrownBy(() -> plan.updateDetails(null, newPricing, newQuotas))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> plan.updateDetails("Go Valid", null, newQuotas))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> plan.updateDetails("Go Valid", newPricing, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should manage plan features and toggle activation status")
    void shouldManageFeaturesAndActivation() {
        SubscriptionPlan plan = SubscriptionPlan.create(
                StripePriceId.of("price_max_monthly"),
                "Max Enterprise",
                PlanTier.MAX,
                PlanPricing.of(Money.soles(new BigDecimal("599.00")), BillingCycle.MONTHLY),
                TenantQuotaLimits.maxPreset()
        );

        PlanFeature feature = PlanFeature.of(PlanFeatureId.generate(), plan.id(), "FEATURE_OBD2_TELEMETRY", "Live telemetry", true);
        plan.addFeature(feature);

        // Feature presence and case-insensitivity
        assertThat(plan.hasFeature("FEATURE_OBD2_TELEMETRY")).isTrue();
        assertThat(plan.hasFeature("feature_obd2_telemetry")).isTrue();
        assertThat(plan.hasFeature("FEATURE_UNKNOWN")).isFalse();
        assertThat(plan.hasFeature(null)).isFalse();

        // Disabling feature
        feature.disable();
        assertThat(plan.hasFeature("FEATURE_OBD2_TELEMETRY")).isFalse();
        feature.enable();
        assertThat(plan.hasFeature("FEATURE_OBD2_TELEMETRY")).isTrue();

        // Replacing feature with same key
        PlanFeature updatedFeature = PlanFeature.of(PlanFeatureId.generate(), plan.id(), "FEATURE_OBD2_TELEMETRY", "Updated Telemetry", true);
        plan.addFeature(updatedFeature);
        assertThat(plan.features()).hasSize(1);
        assertThat(plan.features().get(0).description()).isEqualTo("Updated Telemetry");

        // Null feature rejection
        assertThatThrownBy(() -> plan.addFeature(null))
                .isInstanceOf(NullPointerException.class);

        // Removing features
        plan.removeFeature(null); // Safe no-op
        assertThat(plan.features()).hasSize(1);
        plan.removeFeature("FEATURE_OBD2_TELEMETRY");
        assertThat(plan.features()).isEmpty();

        // Activation toggle
        plan.deactivate();
        assertThat(plan.isActive()).isFalse();

        plan.activate();
        assertThat(plan.isActive()).isTrue();
    }
}
