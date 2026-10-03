package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.exceptions.QuotaExceededException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanFeatureId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.domain.services.SubscriptionQuotaEnforcementService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests covering SubscriptionQuotaEnforcementService and backend-enforced quota governance.
 * Validates branches, staff, work orders, OBD-II devices, photo attachments, company registration,
 * AI reports, multi-warehouse transfers, feature enablement, and inactive subscription rejections.
 *
 * @author Joel Huamani Estefanero
 */
class SubscriptionQuotaEnforcementServiceTest {

    private SubscriptionQuotaEnforcementService service;
    private SubscriptionPlan goPlan;
    private SubscriptionPlan proPlan;
    private SubscriptionPlan maxPlan;
    private SubscriptionPlan enterprisePlan;
    private TenantSubscription activeSub;
    private TenantSubscription canceledSub;
    private TenantSubscription pastDueExpiredSub;

    @BeforeEach
    void setUp() {
        service = new SubscriptionQuotaEnforcementService();

        goPlan = SubscriptionPlan.create(
                StripePriceId.of("price_go"),
                "Go",
                PlanTier.GO,
                PlanPricing.of(Money.soles(new BigDecimal("99")), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPreset()
        );

        proPlan = SubscriptionPlan.create(
                StripePriceId.of("price_pro"),
                "Pro",
                PlanTier.PRO,
                PlanPricing.of(Money.soles(new BigDecimal("299")), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPreset()
        );

        maxPlan = SubscriptionPlan.create(
                StripePriceId.of("price_max"),
                "Max",
                PlanTier.MAX,
                PlanPricing.of(Money.soles(new BigDecimal("599")), BillingCycle.MONTHLY),
                TenantQuotaLimits.maxPreset()
        );

        enterprisePlan = SubscriptionPlan.create(
                StripePriceId.of("price_ent"),
                "Enterprise",
                PlanTier.MAX,
                PlanPricing.of(Money.soles(new BigDecimal("999")), BillingCycle.MONTHLY),
                TenantQuotaLimits.enterprisePreset()
        );

        TenantId tenantId = TenantId.generate();
        SubscriptionPeriod period = SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)));
        activeSub = TenantSubscription.activate(
                tenantId,
                goPlan.id(),
                StripeCustomerId.of("cus_act"),
                StripeSubscriptionId.of("sub_act"),
                period
        );

        canceledSub = TenantSubscription.activate(
                tenantId,
                goPlan.id(),
                StripeCustomerId.of("cus_canc"),
                StripeSubscriptionId.of("sub_canc"),
                period
        );
        canceledSub.cancelImmediately(Instant.now());

        // Past due outside grace period
        SubscriptionPeriod oldPeriod = SubscriptionPeriod.of(Instant.now().minus(Duration.ofDays(30)), Instant.now().minus(Duration.ofDays(6)));
        pastDueExpiredSub = TenantSubscription.activate(
                tenantId,
                goPlan.id(),
                StripeCustomerId.of("cus_exp"),
                StripeSubscriptionId.of("sub_exp"),
                oldPeriod
        );
        pastDueExpiredSub.markPastDue();
    }

    @Test
    @DisplayName("Should validate branch limits accurately including unlimited tiers")
    void shouldValidateBranches() {
        assertThatCode(() -> service.validateBranchCreationAllowed(activeSub, goPlan, 0))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.validateBranchCreationAllowed(activeSub, goPlan, 1))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Branch limit reached");

        assertThatCode(() -> service.validateBranchCreationAllowed(activeSub, proPlan, 1))
                .doesNotThrowAnyException();

        // Unlimited branches in Enterprise plan (-1)
        assertThatCode(() -> service.validateBranchCreationAllowed(activeSub, enterprisePlan, 100))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate staff capacity accurately including unlimited tiers")
    void shouldValidateStaff() {
        assertThatCode(() -> service.validateStaffAdditionAllowed(activeSub, goPlan, 2))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.validateStaffAdditionAllowed(activeSub, goPlan, 3))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Staff limit reached");

        // Unlimited staff in Enterprise plan (-1)
        assertThatCode(() -> service.validateStaffAdditionAllowed(activeSub, enterprisePlan, 500))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate work order monthly quotas including unlimited tiers")
    void shouldValidateWorkOrders() {
        assertThatCode(() -> service.validateWorkOrderCreationAllowed(activeSub, goPlan, 49))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.validateWorkOrderCreationAllowed(activeSub, goPlan, 50))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Monthly work order quota reached");

        // Unlimited work orders in Enterprise plan (-1)
        assertThatCode(() -> service.validateWorkOrderCreationAllowed(activeSub, enterprisePlan, 10000))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate OBD-II dongles registration and telemetry flags")
    void shouldValidateObd2Devices() {
        assertThatThrownBy(() -> service.validateObd2DeviceRegistrationAllowed(activeSub, goPlan, 0))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Live OBD-II IoT telemetry is not enabled");

        assertThatCode(() -> service.validateObd2DeviceRegistrationAllowed(activeSub, proPlan, 4))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.validateObd2DeviceRegistrationAllowed(activeSub, proPlan, 5))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Active OBD-II dongle quota reached");

        // Unlimited OBD-II dongles in Enterprise plan (-1)
        assertThatCode(() -> service.validateObd2DeviceRegistrationAllowed(activeSub, enterprisePlan, 100))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate photo attachments per work order quota")
    void shouldValidatePhotoAttachments() {
        // Go plan allows 5 photos
        assertThatCode(() -> service.validatePhotoUploadAllowed(activeSub, goPlan, 4))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.validatePhotoUploadAllowed(activeSub, goPlan, 5))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Photo attachment limit reached");

        // Max plan allows unlimited photos (-1)
        assertThatCode(() -> service.validatePhotoUploadAllowed(activeSub, maxPlan, 200))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate corporate entity registration permission")
    void shouldValidateCorporateRegistration() {
        assertThatThrownBy(() -> service.validateCompanyCustomerRegistrationAllowed(activeSub, goPlan))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("corporate business customers");

        assertThatCode(() -> service.validateCompanyCustomerRegistrationAllowed(activeSub, proPlan))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate AI executive report quotas")
    void shouldValidateAiReports() {
        assertThatThrownBy(() -> service.validateAiReportGenerationAllowed(activeSub, goPlan, 0))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Predictive AI-assisted Vehicle Health Reports require");

        assertThatCode(() -> service.validateAiReportGenerationAllowed(activeSub, proPlan, 4))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.validateAiReportGenerationAllowed(activeSub, proPlan, 5))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Monthly predictive AI reports quota reached");

        // Max plan allows up to 50 reports
        assertThatCode(() -> service.validateAiReportGenerationAllowed(activeSub, maxPlan, 49))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.validateAiReportGenerationAllowed(activeSub, maxPlan, 50))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Monthly predictive AI reports quota reached");
    }

    @Test
    @DisplayName("Should validate multi-warehouse inventory transfer permission")
    void shouldValidateMultiWarehouse() {
        assertThatThrownBy(() -> service.validateMultiWarehouseTransferAllowed(activeSub, proPlan))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Multi-warehouse inventory routing");

        assertThatCode(() -> service.validateMultiWarehouseTransferAllowed(activeSub, maxPlan))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject any operation when subscription is inactive, canceled, past due expired, or null")
    void shouldRejectOnInactiveSubscription() {
        // Canceled
        assertThatThrownBy(() -> service.validateBranchCreationAllowed(canceledSub, goPlan, 0))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Workshop tenant subscription is inactive");

        // Past due past grace period
        assertThatThrownBy(() -> service.validateStaffAdditionAllowed(pastDueExpiredSub, goPlan, 0))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Workshop tenant subscription is inactive");

        // Null subscription
        assertThatThrownBy(() -> service.validateBranchCreationAllowed(null, goPlan, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should verify feature enablement checking subscription status and plan features")
    void shouldCheckFeatureEnablement() {
        PlanFeature feature = PlanFeature.of(PlanFeatureId.generate(), proPlan.id(), "FEATURE_TELEMETRY", "Telemetry", true);
        proPlan.addFeature(feature);

        // Active subscription + enabled feature
        assertThat(service.isFeatureEnabled(activeSub, proPlan, "FEATURE_TELEMETRY")).isTrue();

        // Unknown feature
        assertThat(service.isFeatureEnabled(activeSub, proPlan, "FEATURE_UNKNOWN")).isFalse();

        // Disabled feature
        feature.disable();
        assertThat(service.isFeatureEnabled(activeSub, proPlan, "FEATURE_TELEMETRY")).isFalse();

        // Inactive subscription
        assertThat(service.isFeatureEnabled(canceledSub, proPlan, "FEATURE_TELEMETRY")).isFalse();

        // Null subscription or plan
        assertThat(service.isFeatureEnabled(null, proPlan, "FEATURE_TELEMETRY")).isFalse();
        assertThat(service.isFeatureEnabled(activeSub, null, "FEATURE_TELEMETRY")).isFalse();
    }
}
