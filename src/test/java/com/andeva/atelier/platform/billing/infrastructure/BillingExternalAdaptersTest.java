package com.andeva.atelier.platform.billing.infrastructure;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.IamTenantValidationAclPort;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.infrastructure.external.acl.iam.IamTenantValidationAdapter;
import com.andeva.atelier.platform.billing.infrastructure.external.cache.caffeine.CaffeineBillingCacheAdapter;
import com.andeva.atelier.platform.billing.infrastructure.external.mail.resend.ResendBillingNotificationAdapter;
import com.andeva.atelier.platform.billing.infrastructure.external.messaging.outbox.BillingOutboxMessageRelayAdapter;
import com.andeva.atelier.platform.billing.infrastructure.external.payment.stripe.StripeWebhookSignatureVerifierAdapter;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.TenantAclDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for Billing External Adapters.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Billing External Adapters Tests")
class BillingExternalAdaptersTest {

    @Mock
    private TenancyContextFacade tenancyContextFacade;
    @Mock
    private OutboxMessageJpaRepository outboxRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("CaffeineBillingCacheAdapter should cache, retrieve, and evict tenant subscription status")
    void testCaffeineBillingCacheAdapter() {
        CaffeineBillingCacheAdapter cacheAdapter = new CaffeineBillingCacheAdapter();
        TenantId tenantId = TenantId.generate();

        assertThat(cacheAdapter.isSubscriptionActive(tenantId)).isEmpty();

        cacheAdapter.cacheSubscriptionActive(tenantId, true);
        assertThat(cacheAdapter.isSubscriptionActive(tenantId)).contains(true);

        cacheAdapter.evict(tenantId);
        assertThat(cacheAdapter.isSubscriptionActive(tenantId)).isEmpty();
    }

    @Test
    @DisplayName("IamTenantValidationAdapter should resolve tenant existence and legal details")
    void testIamTenantValidationAdapter() {
        IamTenantValidationAdapter adapter = new IamTenantValidationAdapter(tenancyContextFacade);
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);

        TenantAclDto dto = new TenantAclDto(tenantUuid, "Talleres Lima", "Talleres Lima S.A.C.", "20123456789", "ACTIVE");
        when(tenancyContextFacade.fetchTenantById(tenantUuid)).thenReturn(Optional.of(dto));

        assertThat(adapter.tenantExists(tenantId)).isTrue();
        assertThat(adapter.getTenantLegalName(tenantId)).isEqualTo("Talleres Lima S.A.C.");
        assertThat(adapter.getTenantContactEmail(tenantId)).contains("20123456789");

        TenantId nonExistent = TenantId.generate();
        when(tenancyContextFacade.fetchTenantById(nonExistent.value())).thenReturn(Optional.empty());
        assertThat(adapter.tenantExists(nonExistent)).isFalse();
        assertThat(adapter.getTenantLegalName(nonExistent)).contains("Workshop");
    }

    @Test
    @DisplayName("ResendBillingNotificationAdapter should execute notification calls without exception")
    void testResendBillingNotificationAdapter() {
        IamTenantValidationAclPort aclPort = mock(IamTenantValidationAclPort.class);
        TenantId tenantId = TenantId.generate();
        when(aclPort.getTenantContactEmail(tenantId)).thenReturn("workshop@atelier.pe");

        ResendBillingNotificationAdapter adapter = new ResendBillingNotificationAdapter(
                aclPort, "re_mock_key", "billing@atelier.pe"
        );

        adapter.sendSubscriptionActivatedNotification(tenantId, "PRO Plan", Instant.now().plusSeconds(3600));
        adapter.sendSubscriptionRenewedNotification(tenantId, "PRO Plan", Instant.now().plusSeconds(86400));
        adapter.sendPaymentFailedNotification(tenantId, "Card expired", "https://portal.stripe.com");

        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                tenantId,
                new StripeInvoiceId("in_111"),
                Money.of(new BigDecimal("120.00"), Currency.USD),
                "https://pdf",
                "https://url",
                Instant.now()
        );
        adapter.sendInvoiceReceipt(tenantId, invoice);

        verify(aclPort, times(4)).getTenantContactEmail(tenantId);
    }

    @Test
    @DisplayName("BillingOutboxMessageRelayAdapter should process and update pending outbox messages")
    void testBillingOutboxMessageRelayAdapter() {
        BillingOutboxMessageRelayAdapter relay = new BillingOutboxMessageRelayAdapter(outboxRepository, eventPublisher);

        OutboxMessagePersistenceEntity msg = new OutboxMessagePersistenceEntity();
        msg.setId(UUID.randomUUID());
        msg.setAggregateType("TenantSubscription");
        msg.setEventType("TenantSubscriptionActivatedIntegrationEvent");
        msg.setStatus(OutboxStatus.PENDING);
        msg.setPayload("{}");
        msg.setOccurredOn(Instant.now());

        when(outboxRepository.findTop50ByStatusAndAggregateTypeOrderByOccurredOnAsc(OutboxStatus.PENDING, "TenantSubscription"))
                .thenReturn(List.of(msg));

        relay.relayPendingBillingMessages();

        assertThat(msg.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        verify(eventPublisher).publishEvent(msg);
        verify(outboxRepository).save(msg);
    }

    @Test
    @DisplayName("StripeWebhookSignatureVerifierAdapter should reject invalid headers safely")
    void testStripeWebhookSignatureVerifierAdapter() {
        StripeWebhookSignatureVerifierAdapter verifier = new StripeWebhookSignatureVerifierAdapter("whsec_test_secret");
        boolean result = verifier.verifySignature("payload", "invalid_header", 300L);
        assertThat(result).isFalse();
    }
}
