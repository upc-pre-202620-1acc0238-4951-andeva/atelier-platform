package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.commandservices.StripeWebhookCommandService;
import com.andeva.atelier.platform.billing.application.internal.commandservices.StripeWebhookCommandServiceImpl;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeGatewayPort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.TenantBillingNotificationGatewayPort;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidWebhookSignatureException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.commands.ProcessStripeWebhookCommand;
import com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.repositories.SaasInvoiceRepository;
import com.andeva.atelier.platform.billing.domain.repositories.StripeWebhookEventRepository;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeWebhookSignatureVerificationPort;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionStatusChangedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link StripeWebhookCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StripeWebhook Command Service Tests")
class StripeWebhookCommandServiceTest {

    @Mock
    private StripeWebhookSignatureVerificationPort signatureVerificationPort;
    @Mock
    private StripeWebhookEventRepository webhookEventRepository;
    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionPlanRepository planRepository;
    @Mock
    private SaasInvoiceRepository invoiceRepository;
    @Mock
    private BillingCachePort billingCachePort;
    @Mock
    private TenantBillingNotificationGatewayPort notificationGatewayPort;
    @Mock
    private StripeGatewayPort stripeGatewayPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private StripeWebhookCommandService webhookCommandService;

    @BeforeEach
    void setUp() {
        webhookCommandService = new StripeWebhookCommandServiceImpl(
                signatureVerificationPort,
                webhookEventRepository,
                subscriptionRepository,
                planRepository,
                invoiceRepository,
                billingCachePort,
                notificationGatewayPort,
                stripeGatewayPort,
                objectMapper,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should throw InvalidWebhookSignatureException when cryptographic verification fails")
    void shouldThrowWhenSignatureInvalid() {
        String payload = "{\"id\":\"evt_123\",\"type\":\"ping\"}";
        String sigHeader = "bad_signature";

        doThrow(new InvalidWebhookSignatureException("Invalid signature"))
                .when(signatureVerificationPort).verifyOrThrow(eq(payload), eq(sigHeader), eq(StripeWebhookSignatureVerificationPort.DEFAULT_TOLERANCE_SECONDS));

        ProcessStripeWebhookCommand command = new ProcessStripeWebhookCommand(payload, sigHeader);

        assertThatThrownBy(() -> webhookCommandService.handle(command))
                .isInstanceOf(InvalidWebhookSignatureException.class);

        verify(webhookEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip processing if Stripe event ID already exists in repository (idempotency shield)")
    void shouldSkipDuplicateEvent() {
        String payload = "{\"id\":\"evt_duplicate_123\",\"type\":\"invoice.payment_succeeded\"}";
        String sigHeader = "valid_sig";

        when(webhookEventRepository.existsByStripeEventId(new StripeEventId("evt_duplicate_123")))
                .thenReturn(true);

        ProcessStripeWebhookCommand command = new ProcessStripeWebhookCommand(payload, sigHeader);
        webhookCommandService.handle(command);

        verify(signatureVerificationPort).verifyOrThrow(payload, sigHeader, StripeWebhookSignatureVerificationPort.DEFAULT_TOLERANCE_SECONDS);
        verify(webhookEventRepository, never()).save(any());
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Should handle checkout.session.completed and activate subscription")
    void shouldHandleCheckoutSessionCompleted() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        String payload = """
                {
                  "id": "evt_checkout_1",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "client_reference_id": "%s",
                      "subscription": "sub_stripe_123",
                      "customer": "cus_stripe_123"
                    }
                  }
                }
                """.formatted(tenantUuid);

        TenantSubscription existing = TenantSubscription.startTrial(
                tenantId,
                PlanId.generate(),
                new StripeCustomerId("cus_stripe_123"),
                14
        );

        when(webhookEventRepository.existsByStripeEventId(any())).thenReturn(false);
        when(webhookEventRepository.save(any(StripeWebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(existing));

        ProcessStripeWebhookCommand command = new ProcessStripeWebhookCommand(payload, "sig_ok");
        webhookCommandService.handle(command);

        assertThat(existing.isAccessGranted()).isTrue();
        verify(subscriptionRepository).save(existing);
        verify(billingCachePort).evict(tenantId);
        verify(eventPublisher).publishEvent(any(TenantSubscriptionStatusChangedIntegrationEvent.class));
    }

    @Test
    @DisplayName("Should handle invoice.payment_succeeded by renewing subscription and saving invoice")
    void shouldHandleInvoicePaymentSucceeded() {
        TenantId tenantId = TenantId.generate();
        StripeSubscriptionId stripeSubId = new StripeSubscriptionId("sub_renew_123");

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                PlanId.generate(),
                new StripeCustomerId("cus_123"),
                stripeSubId,
                SubscriptionPeriod.of(Instant.now().minus(Duration.ofDays(30)), Instant.now())
        );

        String payload = """
                {
                  "id": "evt_inv_paid_1",
                  "type": "invoice.payment_succeeded",
                  "data": {
                    "object": {
                      "id": "in_stripe_999",
                      "subscription": "sub_renew_123",
                      "amount_paid": 9900,
                      "currency": "usd",
                      "invoice_pdf": "https://stripe.com/invoice.pdf",
                      "hosted_invoice_url": "https://stripe.com/invoice/hosted",
                      "lines": {
                        "data": [
                          {
                            "period": {
                              "start": %d,
                              "end": %d
                            }
                          }
                        ]
                      }
                    }
                  }
                }
                """.formatted(Instant.now().getEpochSecond(), Instant.now().plus(Duration.ofDays(30)).getEpochSecond());

        when(webhookEventRepository.existsByStripeEventId(any())).thenReturn(false);
        when(webhookEventRepository.save(any(StripeWebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.findByStripeSubscriptionId(stripeSubId)).thenReturn(Optional.of(subscription));

        ProcessStripeWebhookCommand command = new ProcessStripeWebhookCommand(payload, "sig_ok");
        webhookCommandService.handle(command);

        verify(subscriptionRepository).save(subscription);
        verify(invoiceRepository).save(any(SaasInvoice.class));
        verify(notificationGatewayPort).sendInvoiceReceipt(eq(tenantId), any(SaasInvoice.class));
        verify(billingCachePort).evict(tenantId);
    }

    @Test
    @DisplayName("Should handle invoice.payment_failed by marking subscription past due")
    void shouldHandleInvoicePaymentFailed() {
        TenantId tenantId = TenantId.generate();
        StripeSubscriptionId stripeSubId = new StripeSubscriptionId("sub_fail_123");

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                PlanId.generate(),
                new StripeCustomerId("cus_123"),
                stripeSubId,
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        String payload = """
                {
                  "id": "evt_inv_failed_1",
                  "type": "invoice.payment_failed",
                  "data": {
                    "object": {
                      "subscription": "sub_fail_123"
                    }
                  }
                }
                """;

        when(webhookEventRepository.existsByStripeEventId(any())).thenReturn(false);
        when(webhookEventRepository.save(any(StripeWebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.findByStripeSubscriptionId(stripeSubId)).thenReturn(Optional.of(subscription));

        ProcessStripeWebhookCommand command = new ProcessStripeWebhookCommand(payload, "sig_ok");
        webhookCommandService.handle(command);

        assertThat(subscription.status()).isEqualTo(com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus.PAST_DUE);
        verify(subscriptionRepository).save(subscription);
        verify(billingCachePort).evict(tenantId);
        verify(eventPublisher).publishEvent(any(TenantSubscriptionStatusChangedIntegrationEvent.class));
    }

    @Test
    @DisplayName("Should mark unknown Stripe event types as IGNORED")
    void shouldMarkUnknownEventAsIgnored() {
        String payload = """
                {
                  "id": "evt_unknown_1",
                  "type": "radar.early_fraud_warning.created",
                  "data": {
                    "object": {}
                  }
                }
                """;

        when(webhookEventRepository.existsByStripeEventId(any())).thenReturn(false);
        when(webhookEventRepository.save(any(StripeWebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcessStripeWebhookCommand command = new ProcessStripeWebhookCommand(payload, "sig_ok");
        webhookCommandService.handle(command);

        ArgumentCaptor<StripeWebhookEvent> captor = ArgumentCaptor.forClass(StripeWebhookEvent.class);
        verify(webhookEventRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(WebhookProcessingStatus.IGNORED);
    }

    @Test
    @DisplayName("Should handle customer.subscription.deleted by canceling subscription immediately")
    void shouldHandleCustomerSubscriptionDeleted() {
        TenantId tenantId = TenantId.generate();
        StripeSubscriptionId stripeSubId = new StripeSubscriptionId("sub_cancel_123");

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                PlanId.generate(),
                new StripeCustomerId("cus_123"),
                stripeSubId,
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        String payload = """
                {
                  "id": "evt_sub_del_1",
                  "type": "customer.subscription.deleted",
                  "data": {
                    "object": {
                      "id": "sub_cancel_123"
                    }
                  }
                }
                """;

        when(webhookEventRepository.existsByStripeEventId(any())).thenReturn(false);
        when(webhookEventRepository.save(any(StripeWebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.findByStripeSubscriptionId(stripeSubId)).thenReturn(Optional.of(subscription));

        ProcessStripeWebhookCommand command = new ProcessStripeWebhookCommand(payload, "sig_ok");
        webhookCommandService.handle(command);

        assertThat(subscription.status()).isEqualTo(com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus.CANCELED);
        verify(subscriptionRepository).save(subscription);
        verify(billingCachePort).evict(tenantId);
        verify(eventPublisher).publishEvent(any(com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionSuspendedIntegrationEvent.class));
        verify(eventPublisher).publishEvent(any(com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionStatusChangedIntegrationEvent.class));
    }
}
