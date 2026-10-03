package com.andeva.atelier.platform.billing.application.internal.eventhandlers;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeGatewayPort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.TenantBillingNotificationGatewayPort;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.events.SaasInvoicePaymentFailedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantPlanChangedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionActivatedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionCanceledEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionPastDueEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionRenewedEvent;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Domain Event Handler coordinating cache evictions, notifications,
 * and operational side effects in response to internal Billing domain mutations.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SubscriptionDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionDomainEventHandler.class);

    private final BillingCachePort billingCachePort;
    private final TenantBillingNotificationGatewayPort notificationGatewayPort;
    private final StripeGatewayPort stripeGatewayPort;
    private final SubscriptionPlanRepository planRepository;
    private final TenantSubscriptionRepository subscriptionRepository;

    public SubscriptionDomainEventHandler(
            BillingCachePort billingCachePort,
            TenantBillingNotificationGatewayPort notificationGatewayPort,
            StripeGatewayPort stripeGatewayPort,
            SubscriptionPlanRepository planRepository,
            TenantSubscriptionRepository subscriptionRepository
    ) {
        this.billingCachePort = Objects.requireNonNull(billingCachePort, "BillingCachePort cannot be null");
        this.notificationGatewayPort = Objects.requireNonNull(notificationGatewayPort, "TenantBillingNotificationGatewayPort cannot be null");
        this.stripeGatewayPort = Objects.requireNonNull(stripeGatewayPort, "StripeGatewayPort cannot be null");
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
        this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository, "TenantSubscriptionRepository cannot be null");
    }

    @EventListener
    public void on(TenantSubscriptionActivatedEvent event) {
        log.info("Handling TenantSubscriptionActivatedEvent for tenant: {}", event.tenantId().value());
        evictCaches(event.tenantId());

        String planName = planRepository.findById(event.planId())
                .map(SubscriptionPlan::name)
                .orElse("Atelier Plan");

        notificationGatewayPort.sendSubscriptionActivatedNotification(
                event.tenantId(),
                planName,
                event.expiresAt()
        );
    }

    @EventListener
    public void on(TenantSubscriptionRenewedEvent event) {
        log.info("Handling TenantSubscriptionRenewedEvent for tenant: {}", event.tenantId().value());
        evictCaches(event.tenantId());

        Optional<TenantSubscription> subOpt = subscriptionRepository.findById(event.subscriptionId());
        String planName = subOpt
                .flatMap(s -> planRepository.findById(s.planId()))
                .map(SubscriptionPlan::name)
                .orElse("Atelier Subscription");

        notificationGatewayPort.sendSubscriptionRenewedNotification(
                event.tenantId(),
                planName,
                event.newPeriodEnd()
        );
    }

    @EventListener
    public void on(TenantSubscriptionPastDueEvent event) {
        log.warn("Handling TenantSubscriptionPastDueEvent for tenant: {}", event.tenantId().value());
        evictCaches(event.tenantId());

        Optional<TenantSubscription> subOpt = subscriptionRepository.findById(event.subscriptionId());
        String portalUrl = subOpt
                .map(s -> stripeGatewayPort.createCustomerPortalSession(s.stripeCustomerId(), "https://atelier.andeva.com/dashboard/billing"))
                .orElse("https://atelier.andeva.com/dashboard/billing");

        notificationGatewayPort.sendPaymentFailedNotification(
                event.tenantId(),
                "Subscription payment is past due. Grace period ends on " + event.gracePeriodEnd(),
                portalUrl
        );
    }

    @EventListener
    public void on(TenantSubscriptionCanceledEvent event) {
        log.info("Handling TenantSubscriptionCanceledEvent for tenant: {}", event.tenantId().value());
        evictCaches(event.tenantId());
    }

    @EventListener
    public void on(TenantPlanChangedEvent event) {
        log.info("Handling TenantPlanChangedEvent for tenant: {} (from {} to {})",
                event.tenantId().value(), event.oldPlanId().value(), event.newPlanId().value());
        evictCaches(event.tenantId());
    }

    @EventListener
    public void on(SaasInvoicePaymentFailedEvent event) {
        log.warn("Handling SaasInvoicePaymentFailedEvent for invoice: {}, tenant: {}",
                event.invoiceId().value(), event.tenantId().value());
        evictCaches(event.tenantId());
    }

    private void evictCaches(com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId) {
        if (tenantId != null) {
            billingCachePort.evict(tenantId);
        }
    }
}
