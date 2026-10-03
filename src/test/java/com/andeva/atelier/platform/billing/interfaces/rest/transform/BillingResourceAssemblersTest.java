package com.andeva.atelier.platform.billing.interfaces.rest.transform;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanFeatureId;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.PlanFeatureResource;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SaasInvoiceResource;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SaasInvoiceSummaryResource;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SubscriptionPlanResource;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.TenantSubscriptionResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for SaaS Billing REST resource assemblers.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Billing Resource Assemblers Unit Tests")
class BillingResourceAssemblersTest {

    private PlanFeatureResourceAssembler featureAssembler;
    private SubscriptionPlanResourceAssembler planAssembler;
    private TenantSubscriptionResourceAssembler subscriptionAssembler;
    private SaasInvoiceResourceAssembler invoiceAssembler;

    @BeforeEach
    void setUp() {
        featureAssembler = new PlanFeatureResourceAssembler();
        planAssembler = new SubscriptionPlanResourceAssembler(featureAssembler);
        subscriptionAssembler = new TenantSubscriptionResourceAssembler();
        invoiceAssembler = new SaasInvoiceResourceAssembler();
    }

    @Test
    @DisplayName("PlanFeatureResourceAssembler converts domain entity to REST resource")
    void planFeatureAssemblerConvertsCorrectly() {
        PlanFeature feature = new PlanFeature(
                PlanFeatureId.generate(),
                PlanId.generate(),
                "FEATURE_AI_DIAGNOSTICS",
                "Advanced predictive fault code analysis",
                true
        );

        PlanFeatureResource resource = featureAssembler.toResource(feature);

        assertThat(resource.id()).isEqualTo(feature.id().value());
        assertThat(resource.featureKey()).isEqualTo("FEATURE_AI_DIAGNOSTICS");
        assertThat(resource.description()).isEqualTo("Advanced predictive fault code analysis");
        assertThat(resource.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("SubscriptionPlanResourceAssembler converts domain aggregate with quotas and features")
    void subscriptionPlanAssemblerConvertsCorrectly() {
        SubscriptionPlan plan = SubscriptionPlan.create(
                new StripePriceId("price_pro_month"),
                "Plan Pro",
                PlanTier.PRO,
                new PlanPricing(Money.of(new BigDecimal("129.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset()
        );

        plan.addFeature(new PlanFeature(
                PlanFeatureId.generate(),
                plan.id(),
                "FEATURE_OBD2_TELEMETRY",
                "Live sensor telemetry streams",
                true
        ));

        SubscriptionPlanResource resource = planAssembler.toResource(plan);

        assertThat(resource.id()).isEqualTo(plan.id().value());
        assertThat(resource.stripePriceId()).isEqualTo("price_pro_month");
        assertThat(resource.name()).isEqualTo("Plan Pro");
        assertThat(resource.tier()).isEqualTo("PRO");
        assertThat(resource.price()).isEqualByComparingTo(new BigDecimal("129.00"));
        assertThat(resource.currency()).isEqualTo("USD");
        assertThat(resource.billingCycle()).isEqualTo("MONTHLY");
        assertThat(resource.quotaLimits().maxBranches()).isEqualTo(2);
        assertThat(resource.quotaLimits().maxActiveStaff()).isEqualTo(10);
        assertThat(resource.features()).hasSize(1);
        assertThat(resource.features().getFirst().featureKey()).isEqualTo("FEATURE_OBD2_TELEMETRY");
        assertThat(resource.isActive()).isTrue();
    }

    @Test
    @DisplayName("TenantSubscriptionResourceAssembler converts subscription with plan details")
    void tenantSubscriptionAssemblerConvertsCorrectly() {
        TenantId tenantId = TenantId.generate();
        SubscriptionPlan plan = SubscriptionPlan.create(
                new StripePriceId("price_enterprise_month"),
                "Plan Enterprise",
                PlanTier.ENTERPRISE,
                new PlanPricing(Money.of(new BigDecimal("349.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.enterprisePlanPreset()
        );

        Instant now = Instant.now();
        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                plan.id(),
                new StripeCustomerId("cus_ent_123"),
                new StripeSubscriptionId("sub_ent_123"),
                new SubscriptionPeriod(now, now.plus(30, ChronoUnit.DAYS))
        );

        TenantSubscriptionResource resource = subscriptionAssembler.toResource(subscription, plan);

        assertThat(resource.id()).isEqualTo(subscription.id().value());
        assertThat(resource.tenantId()).isEqualTo(tenantId.value());
        assertThat(resource.planId()).isEqualTo(plan.id().value());
        assertThat(resource.planName()).isEqualTo("Plan Enterprise");
        assertThat(resource.tier()).isEqualTo("ENTERPRISE");
        assertThat(resource.status()).isEqualTo("ACTIVE");
        assertThat(resource.cancelAtPeriodEnd()).isFalse();
        assertThat(resource.quotaLimits().maxBranches()).isEqualTo(-1);
        assertThat(resource.quotaLimits().companyRegistrationAllowed()).isTrue();
        assertThat(resource.isAccessGranted()).isTrue();
    }

    @Test
    @DisplayName("TenantSubscriptionResourceAssembler handles null plan gracefully")
    void tenantSubscriptionAssemblerHandlesNullPlan() {
        TenantId tenantId = TenantId.generate();
        Instant now = Instant.now();
        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                SubscriptionPlan.create(
                        new StripePriceId("price_test_p1"), "P1", PlanTier.GO,
                        new PlanPricing(Money.of(BigDecimal.ONE, Currency.USD), BillingCycle.MONTHLY),
                        TenantQuotaLimits.goPlanPreset()
                ).id(),
                new StripeCustomerId("cus_1"),
                new StripeSubscriptionId("sub_1"),
                new SubscriptionPeriod(now, now.plus(30, ChronoUnit.DAYS))
        );

        TenantSubscriptionResource resource = subscriptionAssembler.toResource(subscription, null);

        assertThat(resource.planName()).isEqualTo("Plan " + subscription.planId().value());
        assertThat(resource.tier()).isEqualTo("UNKNOWN");
        assertThat(resource.quotaLimits()).isNull();
    }

    @Test
    @DisplayName("SaasInvoiceResourceAssembler converts full and summary invoice representations")
    void saasInvoiceAssemblerConvertsCorrectly() {
        TenantId tenantId = TenantId.generate();
        Instant paidTime = Instant.now();
        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                tenantId,
                new StripeInvoiceId("in_test_invoice_001"),
                Money.of(new BigDecimal("129.00"), Currency.USD),
                "https://stripe.com/pdf/in_001",
                "https://stripe.com/hosted/in_001",
                paidTime
        );

        SaasInvoiceResource detailed = invoiceAssembler.toResource(invoice);
        assertThat(detailed.id()).isEqualTo(invoice.id().value());
        assertThat(detailed.stripeInvoiceId()).isEqualTo("in_test_invoice_001");
        assertThat(detailed.amountPaid()).isEqualByComparingTo(new BigDecimal("129.00"));
        assertThat(detailed.currency()).isEqualTo("USD");
        assertThat(detailed.status()).isEqualTo("PAID");
        assertThat(detailed.invoicePdfUrl()).isEqualTo("https://stripe.com/pdf/in_001");
        assertThat(detailed.paidAt()).isEqualTo(paidTime);

        SaasInvoiceSummaryResource summary = invoiceAssembler.toSummaryResource(invoice);
        assertThat(summary.id()).isEqualTo(invoice.id().value());
        assertThat(summary.stripeInvoiceId()).isEqualTo("in_test_invoice_001");
        assertThat(summary.amountPaid()).isEqualByComparingTo(new BigDecimal("129.00"));

        List<SaasInvoiceSummaryResource> list = invoiceAssembler.toSummaryResourceList(List.of(invoice));
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Assemblers reject null domain objects")
    void assemblersRejectNull() {
        assertThatThrownBy(() -> featureAssembler.toResource(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> planAssembler.toResource(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> subscriptionAssembler.toResource(null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> invoiceAssembler.toResource(null))
                .isInstanceOf(NullPointerException.class);
    }
}
