package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanFeatureId;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests covering strongly typed IDs and Value Objects in SaaS Billing & Subscriptions.
 * Validates edge cases, boundary conditions, formatting rules, and invariant bounds.
 *
 * @author Joel Huamani Estefanero
 */
class BillingValueObjectsAndIdsTest {

    @Test
    @DisplayName("Should create and validate PlanId, SubscriptionId, SaasInvoiceId, and PlanFeatureId")
    void shouldHandleUuidBasedIds() {
        UUID uuid = UUID.randomUUID();

        PlanId planId = PlanId.of(uuid);
        assertThat(planId.value()).isEqualTo(uuid);
        assertThat(PlanId.of(uuid.toString()).value()).isEqualTo(uuid);
        assertThat(PlanId.generate()).isNotNull();

        SubscriptionId subId = SubscriptionId.of(uuid);
        assertThat(subId.value()).isEqualTo(uuid);
        assertThat(SubscriptionId.of(uuid.toString()).value()).isEqualTo(uuid);
        assertThat(SubscriptionId.generate()).isNotNull();

        SaasInvoiceId invId = SaasInvoiceId.of(uuid);
        assertThat(invId.value()).isEqualTo(uuid);
        assertThat(SaasInvoiceId.of(uuid.toString()).value()).isEqualTo(uuid);
        assertThat(SaasInvoiceId.generate()).isNotNull();

        PlanFeatureId featId = PlanFeatureId.of(uuid);
        assertThat(featId.value()).isEqualTo(uuid);
        assertThat(PlanFeatureId.of(uuid.toString()).value()).isEqualTo(uuid);
        assertThat(PlanFeatureId.generate()).isNotNull();

        // Null UUID handling
        assertThatThrownBy(() -> PlanId.of((UUID) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SubscriptionId.of((UUID) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SaasInvoiceId.of((UUID) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> PlanFeatureId.of((UUID) null))
                .isInstanceOf(NullPointerException.class);

        // Null string handling
        assertThatThrownBy(() -> PlanId.of((String) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SubscriptionId.of((String) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SaasInvoiceId.of((String) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> PlanFeatureId.of((String) null))
                .isInstanceOf(NullPointerException.class);

        // Invalid format string handling
        assertThatThrownBy(() -> PlanId.of("invalid-uuid-string"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SubscriptionId.of("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SaasInvoiceId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PlanFeatureId.of("12345"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should validate StripeEventId prefix and format")
    void shouldValidateStripeEventId() {
        StripeEventId eventId = StripeEventId.of("evt_3Nk23a4b5c6d");
        assertThat(eventId.value()).isEqualTo("evt_3Nk23a4b5c6d");

        assertThatThrownBy(() -> StripeEventId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> StripeEventId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeEventId.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeEventId.of("sub_12345"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeEventId.of("evt_"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeEventId.of("evt_123$!@#"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should validate StripeCustomerId prefix and format")
    void shouldValidateStripeCustomerId() {
        StripeCustomerId customerId = StripeCustomerId.of("cus_12345abcde");
        assertThat(customerId.value()).isEqualTo("cus_12345abcde");

        assertThatThrownBy(() -> StripeCustomerId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> StripeCustomerId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeCustomerId.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeCustomerId.of("invalid_cus"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeCustomerId.of("cus_"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should validate StripeSubscriptionId, StripePriceId, and StripeInvoiceId")
    void shouldValidateStripePrefixedIds() {
        StripeSubscriptionId subId = StripeSubscriptionId.of("sub_98765xyz");
        assertThat(subId.value()).isEqualTo("sub_98765xyz");
        assertThatThrownBy(() -> StripeSubscriptionId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> StripeSubscriptionId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeSubscriptionId.of("price_98765"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeSubscriptionId.of("sub_"))
                .isInstanceOf(IllegalArgumentException.class);

        StripePriceId priceId = StripePriceId.of("price_1N2M3L4K");
        assertThat(priceId.value()).isEqualTo("price_1N2M3L4K");
        assertThatThrownBy(() -> StripePriceId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> StripePriceId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripePriceId.of("in_1N2M3L4K"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripePriceId.of("price_"))
                .isInstanceOf(IllegalArgumentException.class);

        StripeInvoiceId invoiceId = StripeInvoiceId.of("in_1N2M3L4K");
        assertThat(invoiceId.value()).isEqualTo("in_1N2M3L4K");
        assertThatThrownBy(() -> StripeInvoiceId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> StripeInvoiceId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeInvoiceId.of("cus_1N2M3L4K"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StripeInvoiceId.of("in_"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should validate PlanPricing price, zero amount, negative rejection, and billing cycle")
    void shouldValidatePlanPricing() {
        PlanPricing pricing = PlanPricing.of(Money.soles(new BigDecimal("199.00")), BillingCycle.MONTHLY);
        assertThat(pricing.price().amount()).isEqualByComparingTo("199.00");
        assertThat(pricing.billingCycle()).isEqualTo(BillingCycle.MONTHLY);

        // Zero price is valid (free tier)
        PlanPricing freePricing = PlanPricing.of(Money.ZERO_PEN, BillingCycle.MONTHLY);
        assertThat(freePricing.price().amount()).isEqualByComparingTo(BigDecimal.ZERO);

        // Null price rejection
        assertThatThrownBy(() -> PlanPricing.of(null, BillingCycle.MONTHLY))
                .isInstanceOf(NullPointerException.class);

        // Null billing cycle rejection
        assertThatThrownBy(() -> PlanPricing.of(Money.soles(BigDecimal.TEN), null))
                .isInstanceOf(NullPointerException.class);

        // Negative price rejection
        assertThatThrownBy(() -> PlanPricing.of(Money.soles(new BigDecimal("-10.00")), BillingCycle.MONTHLY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should validate SubscriptionPeriod duration, boundary inclusion, and chronologic invariants")
    void shouldValidateSubscriptionPeriod() {
        Instant now = Instant.now();
        Instant end = now.plus(Duration.ofDays(30));

        SubscriptionPeriod period = SubscriptionPeriod.of(now, end);
        assertThat(period.duration().toDays()).isEqualTo(30);

        // Boundary tests for isActiveAt
        assertThat(period.isActiveAt(now)).isTrue();
        assertThat(period.isActiveAt(end)).isTrue();
        assertThat(period.isActiveAt(now.plus(Duration.ofDays(10)))).isTrue();
        assertThat(period.isActiveAt(now.minusNanos(1))).isFalse();
        assertThat(period.isActiveAt(end.plusNanos(1))).isFalse();
        assertThat(period.isActiveAt(null)).isFalse();

        // Zero duration period (startDate == endDate) is valid
        SubscriptionPeriod zeroPeriod = SubscriptionPeriod.of(now, now);
        assertThat(zeroPeriod.duration()).isEqualTo(Duration.ZERO);
        assertThat(zeroPeriod.isActiveAt(now)).isTrue();

        // Invariant: startDate after endDate
        assertThatThrownBy(() -> SubscriptionPeriod.of(end, now))
                .isInstanceOf(IllegalArgumentException.class);

        // Null checks
        assertThatThrownBy(() -> SubscriptionPeriod.of(null, end))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SubscriptionPeriod.of(now, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should validate TenantQuotaLimits presets")
    void shouldValidateTenantQuotaLimitsPresets() {
        TenantQuotaLimits go = TenantQuotaLimits.goPreset();
        assertThat(go.maxBranches()).isEqualTo(1);
        assertThat(go.maxActiveStaff()).isEqualTo(3);
        assertThat(go.maxActiveObd2Devices()).isEqualTo(0);
        assertThat(go.maxPhotosPerWorkOrder()).isEqualTo(5);
        assertThat(go.maxMonthlyAiReports()).isEqualTo(0);
        assertThat(go.companyRegistrationAllowed()).isFalse();
        assertThat(go.multiWarehouseAllowed()).isFalse();
        assertThat(go.marketplaceListed()).isFalse();
        assertThat(go.maxMonthlyWorkOrders()).isEqualTo(50);
        assertThat(go.iotTelemetryEnabled()).isFalse();
        assertThat(go.aiDiagnosticsEnabled()).isFalse();

        TenantQuotaLimits pro = TenantQuotaLimits.proPreset();
        assertThat(pro.maxBranches()).isEqualTo(2);
        assertThat(pro.maxActiveStaff()).isEqualTo(10);
        assertThat(pro.maxActiveObd2Devices()).isEqualTo(5);
        assertThat(pro.maxPhotosPerWorkOrder()).isEqualTo(20);
        assertThat(pro.maxMonthlyAiReports()).isEqualTo(5);
        assertThat(pro.companyRegistrationAllowed()).isTrue();
        assertThat(pro.multiWarehouseAllowed()).isFalse();
        assertThat(pro.marketplaceListed()).isTrue();
        assertThat(pro.maxMonthlyWorkOrders()).isEqualTo(200);
        assertThat(pro.iotTelemetryEnabled()).isTrue();
        assertThat(pro.aiDiagnosticsEnabled()).isTrue();

        TenantQuotaLimits max = TenantQuotaLimits.maxPreset();
        assertThat(max.maxBranches()).isEqualTo(5);
        assertThat(max.maxActiveStaff()).isEqualTo(25);
        assertThat(max.maxActiveObd2Devices()).isEqualTo(20);
        assertThat(max.maxPhotosPerWorkOrder()).isEqualTo(-1);
        assertThat(max.maxMonthlyAiReports()).isEqualTo(50);
        assertThat(max.companyRegistrationAllowed()).isTrue();
        assertThat(max.multiWarehouseAllowed()).isTrue();
        assertThat(max.marketplaceListed()).isTrue();
        assertThat(max.maxMonthlyWorkOrders()).isEqualTo(1000);
        assertThat(max.iotTelemetryEnabled()).isTrue();
        assertThat(max.aiDiagnosticsEnabled()).isTrue();

        TenantQuotaLimits enterprise = TenantQuotaLimits.enterprisePreset();
        assertThat(enterprise.maxBranches()).isEqualTo(-1);
        assertThat(enterprise.maxActiveStaff()).isEqualTo(-1);
        assertThat(enterprise.maxActiveObd2Devices()).isEqualTo(-1);
        assertThat(enterprise.maxPhotosPerWorkOrder()).isEqualTo(-1);
        assertThat(enterprise.maxMonthlyAiReports()).isEqualTo(-1);
        assertThat(enterprise.maxMonthlyWorkOrders()).isEqualTo(-1);
        assertThat(enterprise.companyRegistrationAllowed()).isTrue();
        assertThat(enterprise.multiWarehouseAllowed()).isTrue();
        assertThat(enterprise.marketplaceListed()).isTrue();
        assertThat(enterprise.iotTelemetryEnabled()).isTrue();
        assertThat(enterprise.aiDiagnosticsEnabled()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -5, -100})
    @DisplayName("Should reject negative quotas less than -1 for all numeric bounds")
    void shouldRejectInvalidNegativeQuotas(int invalidVal) {
        assertThatThrownBy(() -> new TenantQuotaLimits(invalidVal, 1, 1, 1, 1, false, false, false, 1, false, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantQuotaLimits(1, invalidVal, 1, 1, 1, false, false, false, 1, false, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantQuotaLimits(1, 1, invalidVal, 1, 1, false, false, false, 1, false, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantQuotaLimits(1, 1, 1, invalidVal, 1, false, false, false, 1, false, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantQuotaLimits(1, 1, 1, 1, invalidVal, false, false, false, 1, false, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantQuotaLimits(1, 1, 1, 1, 1, false, false, false, invalidVal, false, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
