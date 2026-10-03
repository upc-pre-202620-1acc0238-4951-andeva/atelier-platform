package com.andeva.atelier.platform.billing.application.internal.commandservices;

import com.andeva.atelier.platform.billing.application.commandservices.StripeWebhookCommandService;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeGatewayPort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.TenantBillingNotificationGatewayPort;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.exceptions.StripeWebhookProcessingException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.commands.ProcessStripeWebhookCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.repositories.SaasInvoiceRepository;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.StripeWebhookEventRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeWebhookSignatureVerificationPort;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionStatusChangedIntegrationEvent;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionSuspendedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of StripeWebhookCommandService processing asynchronous webhook events
 * with HMAC-SHA256 signature verification, Exactly-Once idempotency shielding,
 * and subscription lifecycle transitions.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class StripeWebhookCommandServiceImpl implements StripeWebhookCommandService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookCommandServiceImpl.class);

    private final StripeWebhookSignatureVerificationPort signatureVerificationPort;
    private final StripeWebhookEventRepository webhookEventRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SaasInvoiceRepository invoiceRepository;
    private final BillingCachePort billingCachePort;
    private final TenantBillingNotificationGatewayPort notificationGatewayPort;
    private final StripeGatewayPort stripeGatewayPort;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final StripeWebhookAuditRecorder auditRecorder;

    @org.springframework.beans.factory.annotation.Autowired
    public StripeWebhookCommandServiceImpl(
            StripeWebhookSignatureVerificationPort signatureVerificationPort,
            StripeWebhookEventRepository webhookEventRepository,
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SaasInvoiceRepository invoiceRepository,
            BillingCachePort billingCachePort,
            TenantBillingNotificationGatewayPort notificationGatewayPort,
            StripeGatewayPort stripeGatewayPort,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher,
            StripeWebhookAuditRecorder auditRecorder
    ) {
        this.signatureVerificationPort = Objects.requireNonNull(signatureVerificationPort, "StripeWebhookSignatureVerificationPort cannot be null");
        this.webhookEventRepository = Objects.requireNonNull(webhookEventRepository, "StripeWebhookEventRepository cannot be null");
        this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository, "TenantSubscriptionRepository cannot be null");
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository, "SaasInvoiceRepository cannot be null");
        this.billingCachePort = Objects.requireNonNull(billingCachePort, "BillingCachePort cannot be null");
        this.notificationGatewayPort = Objects.requireNonNull(notificationGatewayPort, "TenantBillingNotificationGatewayPort cannot be null");
        this.stripeGatewayPort = Objects.requireNonNull(stripeGatewayPort, "StripeGatewayPort cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
        this.auditRecorder = auditRecorder != null ? auditRecorder : new StripeWebhookAuditRecorder(webhookEventRepository);
    }

    public StripeWebhookCommandServiceImpl(
            StripeWebhookSignatureVerificationPort signatureVerificationPort,
            StripeWebhookEventRepository webhookEventRepository,
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SaasInvoiceRepository invoiceRepository,
            BillingCachePort billingCachePort,
            TenantBillingNotificationGatewayPort notificationGatewayPort,
            StripeGatewayPort stripeGatewayPort,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this(
                signatureVerificationPort,
                webhookEventRepository,
                subscriptionRepository,
                planRepository,
                invoiceRepository,
                billingCachePort,
                notificationGatewayPort,
                stripeGatewayPort,
                objectMapper,
                eventPublisher,
                new StripeWebhookAuditRecorder(webhookEventRepository)
        );
    }

    @Override
    public void handle(ProcessStripeWebhookCommand command) {
        Objects.requireNonNull(command, "ProcessStripeWebhookCommand cannot be null");

        // 1. Verify cryptographic HMAC-SHA256 signature with replay protection tolerance
        signatureVerificationPort.verifyOrThrow(
                command.payload(),
                command.signatureHeader(),
                StripeWebhookSignatureVerificationPort.DEFAULT_TOLERANCE_SECONDS
        );

        // 2. Parse payload structure
        JsonNode root;
        try {
            root = objectMapper.readTree(command.payload());
        } catch (Exception e) {
            throw new StripeWebhookProcessingException("Failed to parse Stripe webhook JSON payload", e);
        }

        String eventIdStr = root.path("id").asText();
        String eventType = root.path("type").asText();

        if (eventIdStr == null || eventIdStr.isBlank() || eventType == null || eventType.isBlank()) {
            throw new StripeWebhookProcessingException("Stripe webhook payload missing mandatory 'id' or 'type'");
        }

        StripeEventId stripeEventId = new StripeEventId(eventIdStr);

        // 3. Idempotency Shield: return early if already processed or recorded
        if (webhookEventRepository.existsByStripeEventId(stripeEventId)) {
            log.info("Stripe webhook event {} already registered in idempotency log. Skipping duplicate dispatch.", eventIdStr);
            return;
        }

        // 4. Register event in PENDING state
        StripeWebhookEvent webhookEvent = StripeWebhookEvent.receive(stripeEventId, eventType, command.payload());
        webhookEvent = webhookEventRepository.save(webhookEvent);

        try {
            // 5. Business dispatch based on event type
            switch (eventType) {
                case "checkout.session.completed" -> handleCheckoutSessionCompleted(root.path("data").path("object"));
                case "invoice.payment_succeeded" -> handleInvoicePaymentSucceeded(root.path("data").path("object"));
                case "invoice.payment_failed" -> handleInvoicePaymentFailed(root.path("data").path("object"));
                case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted(root.path("data").path("object"));
                default -> {
                    log.info("Ignoring unhandled Stripe event type: {}", eventType);
                    webhookEvent.markIgnored();
                }
            }

            if (webhookEvent.status() != com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus.IGNORED) {
                webhookEvent.markProcessed();
            }
            webhookEventRepository.save(webhookEvent);

        } catch (Exception ex) {
            log.error("Error processing Stripe webhook event {}: {}", eventIdStr, ex.getMessage(), ex);
            auditRecorder.recordFailure(webhookEvent, ex.getMessage());
            throw new StripeWebhookProcessingException("Error executing webhook business logic for event: " + eventIdStr, ex);
        }
    }

    private void handleCheckoutSessionCompleted(JsonNode sessionNode) {
        String tenantIdStr = sessionNode.path("metadata").path("tenant_id").asText();
        if (tenantIdStr.isBlank()) {
            tenantIdStr = sessionNode.path("client_reference_id").asText();
        }

        if (tenantIdStr.isBlank()) {
            log.warn("checkout.session.completed received without tenant identifier");
            return;
        }

        TenantId tenantId = new TenantId(UUID.fromString(tenantIdStr));
        String subIdStr = sessionNode.path("subscription").asText();
        String customerIdStr = sessionNode.path("customer").asText();
        String planIdStr = sessionNode.path("metadata").path("plan_id").asText();

        Instant start = Instant.now();
        Instant end = start.plus(Duration.ofDays(30));
        SubscriptionPeriod period = SubscriptionPeriod.of(start, end);

        Optional<TenantSubscription> existingOpt = subscriptionRepository.findByTenantId(tenantId);
        if (existingOpt.isPresent()) {
            TenantSubscription sub = existingOpt.get();
            String oldStatus = sub.status().name();
            if (!subIdStr.isBlank()) {
                sub.activateFromTrial(new StripeSubscriptionId(subIdStr), period);
            } else {
                sub.renewPeriod(period);
            }
            subscriptionRepository.save(sub);
            billingCachePort.evict(tenantId);
            eventPublisher.publishEvent(TenantSubscriptionStatusChangedIntegrationEvent.of(
                    sub.id().value(),
                    tenantId.value(),
                    oldStatus,
                    sub.status().name()
            ));
        } else if (!planIdStr.isBlank()) {
            SubscriptionPlan plan = planRepository.findById(new PlanId(UUID.fromString(planIdStr)))
                    .orElseThrow(() -> new PlanNotFoundException("Plan not found during checkout activation: " + planIdStr));
            TenantSubscription sub = TenantSubscription.activate(
                    tenantId,
                    plan.id(),
                    new StripeCustomerId(customerIdStr),
                    new StripeSubscriptionId(subIdStr),
                    period
            );
            subscriptionRepository.save(sub);
            billingCachePort.evict(tenantId);
            eventPublisher.publishEvent(TenantSubscriptionStatusChangedIntegrationEvent.of(
                    sub.id().value(),
                    tenantId.value(),
                    "NONE",
                    sub.status().name()
            ));
        }
    }

    private void handleInvoicePaymentSucceeded(JsonNode invoiceNode) {
        String subIdStr = invoiceNode.path("subscription").asText();
        String invoiceIdStr = invoiceNode.path("id").asText();
        long amountPaidCents = invoiceNode.path("amount_paid").asLong(0);
        String currencyStr = invoiceNode.path("currency").asText("USD").toUpperCase();
        String pdfUrl = invoiceNode.path("invoice_pdf").asText("");
        String hostedUrl = invoiceNode.path("hosted_invoice_url").asText("");

        Optional<TenantSubscription> subOpt = Optional.empty();
        if (!subIdStr.isBlank()) {
            subOpt = subscriptionRepository.findByStripeSubscriptionId(new StripeSubscriptionId(subIdStr));
        }

        if (subOpt.isEmpty()) {
            String customerIdStr = invoiceNode.path("customer").asText();
            log.warn("Invoice payment succeeded but no subscription found for Stripe subId: {}, customer: {}", subIdStr, customerIdStr);
            return;
        }

        TenantSubscription sub = subOpt.get();

        long startSec = invoiceNode.path("lines").path("data").path(0).path("period").path("start").asLong(0);
        long endSec = invoiceNode.path("lines").path("data").path(0).path("period").path("end").asLong(0);
        Instant pStart = startSec > 0 ? Instant.ofEpochSecond(startSec) : Instant.now();
        Instant pEnd = endSec > 0 ? Instant.ofEpochSecond(endSec) : pStart.plus(Duration.ofDays(30));
        SubscriptionPeriod renewedPeriod = SubscriptionPeriod.of(pStart, pEnd);

        sub.renewPeriod(renewedPeriod);
        subscriptionRepository.save(sub);
        billingCachePort.evict(sub.tenantId());

        BigDecimal amount = BigDecimal.valueOf(amountPaidCents, 2);
        Currency cur;
        try {
            cur = Currency.valueOf(currencyStr);
        } catch (IllegalArgumentException e) {
            cur = Currency.USD;
        }
        Money moneyPaid = Money.of(amount, cur);

        SaasInvoice invoice = SaasInvoice.recordPaid(
                sub.id(),
                sub.tenantId(),
                new StripeInvoiceId(invoiceIdStr),
                moneyPaid,
                pdfUrl,
                hostedUrl,
                Instant.now()
        );
        invoiceRepository.save(invoice);

        notificationGatewayPort.sendInvoiceReceipt(sub.tenantId(), invoice);
    }

    private void handleInvoicePaymentFailed(JsonNode invoiceNode) {
        String subIdStr = invoiceNode.path("subscription").asText();
        if (subIdStr.isBlank()) {
            return;
        }

        Optional<TenantSubscription> subOpt = subscriptionRepository.findByStripeSubscriptionId(new StripeSubscriptionId(subIdStr));
        if (subOpt.isPresent()) {
            TenantSubscription sub = subOpt.get();
            String oldStatus = sub.status().name();
            sub.markPastDue();
            subscriptionRepository.save(sub);
            billingCachePort.evict(sub.tenantId());

            eventPublisher.publishEvent(TenantSubscriptionStatusChangedIntegrationEvent.of(
                    sub.id().value(),
                    sub.tenantId().value(),
                    oldStatus,
                    sub.status().name()
            ));
        }
    }

    private void handleCustomerSubscriptionDeleted(JsonNode subscriptionNode) {
        String subIdStr = subscriptionNode.path("id").asText();
        if (subIdStr.isBlank()) {
            return;
        }

        Optional<TenantSubscription> subOpt = subscriptionRepository.findByStripeSubscriptionId(new StripeSubscriptionId(subIdStr));
        if (subOpt.isPresent()) {
            TenantSubscription sub = subOpt.get();
            String oldStatus = sub.status().name();
            sub.cancelImmediately(Instant.now());
            subscriptionRepository.save(sub);
            billingCachePort.evict(sub.tenantId());

            eventPublisher.publishEvent(TenantSubscriptionSuspendedIntegrationEvent.of(
                    sub.id().value(),
                    sub.tenantId().value(),
                    "Subscription was terminated via Stripe"
            ));

            eventPublisher.publishEvent(TenantSubscriptionStatusChangedIntegrationEvent.of(
                    sub.id().value(),
                    sub.tenantId().value(),
                    oldStatus,
                    sub.status().name()
            ));
        }
    }
}
