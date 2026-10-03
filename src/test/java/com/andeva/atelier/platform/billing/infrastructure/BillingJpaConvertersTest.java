package com.andeva.atelier.platform.billing.infrastructure;

import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.InvoiceStatus;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.BillingCycleConverter;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.InvoiceStatusConverter;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.PlanTierConverter;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.SubscriptionStatusConverter;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.WebhookProcessingStatusConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for Billing JPA Attribute Converters.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Billing JPA Attribute Converters Tests")
class BillingJpaConvertersTest {

    private final PlanTierConverter planTierConverter = new PlanTierConverter();
    private final SubscriptionStatusConverter subscriptionStatusConverter = new SubscriptionStatusConverter();
    private final InvoiceStatusConverter invoiceStatusConverter = new InvoiceStatusConverter();
    private final BillingCycleConverter billingCycleConverter = new BillingCycleConverter();
    private final WebhookProcessingStatusConverter webhookProcessingStatusConverter = new WebhookProcessingStatusConverter();

    @Test
    @DisplayName("PlanTierConverter should convert between enum and uppercase string")
    void testPlanTierConverter() {
        assertThat(planTierConverter.convertToDatabaseColumn(PlanTier.PRO)).isEqualTo("PRO");
        assertThat(planTierConverter.convertToEntityAttribute("PRO")).isEqualTo(PlanTier.PRO);
        assertThat(planTierConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(planTierConverter.convertToEntityAttribute(null)).isNull();
        assertThat(planTierConverter.convertToEntityAttribute("   ")).isNull();
    }

    @Test
    @DisplayName("SubscriptionStatusConverter should convert between enum and lowercase string")
    void testSubscriptionStatusConverter() {
        assertThat(subscriptionStatusConverter.convertToDatabaseColumn(SubscriptionStatus.ACTIVE)).isEqualTo("active");
        assertThat(subscriptionStatusConverter.convertToEntityAttribute("active")).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscriptionStatusConverter.convertToEntityAttribute("ACTIVE")).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscriptionStatusConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(subscriptionStatusConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("InvoiceStatusConverter should convert between enum and lowercase string")
    void testInvoiceStatusConverter() {
        assertThat(invoiceStatusConverter.convertToDatabaseColumn(InvoiceStatus.PAID)).isEqualTo("paid");
        assertThat(invoiceStatusConverter.convertToEntityAttribute("paid")).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoiceStatusConverter.convertToEntityAttribute("PAID")).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoiceStatusConverter.convertToEntityAttribute("draft")).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoiceStatusConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(invoiceStatusConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("BillingCycleConverter should convert between enum and string")
    void testBillingCycleConverter() {
        assertThat(billingCycleConverter.convertToDatabaseColumn(BillingCycle.MONTHLY)).isEqualTo("MONTHLY");
        assertThat(billingCycleConverter.convertToEntityAttribute("MONTHLY")).isEqualTo(BillingCycle.MONTHLY);
        assertThat(billingCycleConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(billingCycleConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("WebhookProcessingStatusConverter should convert between enum and lowercase string")
    void testWebhookProcessingStatusConverter() {
        assertThat(webhookProcessingStatusConverter.convertToDatabaseColumn(WebhookProcessingStatus.PROCESSED)).isEqualTo("processed");
        assertThat(webhookProcessingStatusConverter.convertToEntityAttribute("processed")).isEqualTo(WebhookProcessingStatus.PROCESSED);
        assertThat(webhookProcessingStatusConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(webhookProcessingStatusConverter.convertToEntityAttribute(null)).isNull();
    }
}
