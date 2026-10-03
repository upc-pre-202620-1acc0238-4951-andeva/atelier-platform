package com.andeva.atelier.platform.billing.application.internal.commandservices;

import com.andeva.atelier.platform.billing.application.commandservices.TenantSubscriptionCommandService;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.IamTenantValidationAclPort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeGatewayPort;
import com.andeva.atelier.platform.billing.domain.exceptions.DuplicateActiveSubscriptionException;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.exceptions.SubscriptionNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.ChangeSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.InitiateCheckoutSessionCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.billing.interfaces.events.TenantPlanUpgradedIntegrationEvent;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionSuspendedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of TenantSubscriptionCommandService orchestrating checkout sessions,
 * plan transitions, subscription cancellations, and trial provisioning.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class TenantSubscriptionCommandServiceImpl implements TenantSubscriptionCommandService {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final StripeGatewayPort stripeGatewayPort;
    private final BillingCachePort billingCachePort;
    private final IamTenantValidationAclPort iamTenantValidationAclPort;
    private final ApplicationEventPublisher eventPublisher;

    public TenantSubscriptionCommandServiceImpl(
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            StripeGatewayPort stripeGatewayPort,
            BillingCachePort billingCachePort,
            IamTenantValidationAclPort iamTenantValidationAclPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository, "TenantSubscriptionRepository cannot be null");
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
        this.stripeGatewayPort = Objects.requireNonNull(stripeGatewayPort, "StripeGatewayPort cannot be null");
        this.billingCachePort = Objects.requireNonNull(billingCachePort, "BillingCachePort cannot be null");
        this.iamTenantValidationAclPort = Objects.requireNonNull(iamTenantValidationAclPort, "IamTenantValidationAclPort cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public String handle(InitiateCheckoutSessionCommand command) {
        Objects.requireNonNull(command, "InitiateCheckoutSessionCommand cannot be null");

        SubscriptionPlan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new PlanNotFoundException(command.planId()));

        if (!plan.isActive()) {
            throw new PlanNotFoundException("Commercial plan is not active for new subscriptions: " + command.planId().value());
        }

        Optional<TenantSubscription> existingOpt = subscriptionRepository.findByTenantId(command.tenantId());
        StripeCustomerId customerId;

        if (existingOpt.isPresent()) {
            TenantSubscription existing = existingOpt.get();
            if (existing.planId().equals(command.planId()) && existing.isAccessGranted()) {
                throw new DuplicateActiveSubscriptionException(command.tenantId());
            }
            customerId = existing.stripeCustomerId();
        } else {
            String legalName = iamTenantValidationAclPort.getTenantLegalName(command.tenantId());
            String email = iamTenantValidationAclPort.getTenantContactEmail(command.tenantId());
            customerId = stripeGatewayPort.createCustomer(command.tenantId(), legalName, email);
        }

        return stripeGatewayPort.createCheckoutSession(
                command.tenantId(),
                plan.id(),
                plan.stripePriceId(),
                customerId,
                command.successUrl(),
                command.cancelUrl()
        );
    }

    @Override
    @Transactional
    public void handle(ChangeSubscriptionPlanCommand command) {
        Objects.requireNonNull(command, "ChangeSubscriptionPlanCommand cannot be null");

        TenantSubscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException(command.subscriptionId()));

        SubscriptionPlan newPlan = planRepository.findById(command.newPlanId())
                .orElseThrow(() -> new PlanNotFoundException(command.newPlanId()));

        PlanId oldPlanId = subscription.planId();
        subscription.changePlan(newPlan.id(), newPlan.stripePriceId());
        subscriptionRepository.save(subscription);

        billingCachePort.evict(subscription.tenantId());

        eventPublisher.publishEvent(TenantPlanUpgradedIntegrationEvent.of(
                subscription.id().value(),
                subscription.tenantId().value(),
                oldPlanId.value(),
                newPlan.id().value(),
                newPlan.name(),
                newPlan.tier().name()
        ));
    }

    @Override
    @Transactional
    public void handle(CancelSubscriptionCommand command) {
        Objects.requireNonNull(command, "CancelSubscriptionCommand cannot be null");

        TenantSubscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException(command.subscriptionId()));

        if (command.cancelImmediately()) {
            subscription.cancelImmediately(Instant.now());
            subscriptionRepository.save(subscription);
            billingCachePort.evict(subscription.tenantId());
            eventPublisher.publishEvent(TenantSubscriptionSuspendedIntegrationEvent.of(
                    subscription.id().value(),
                    subscription.tenantId().value(),
                    "Voluntary immediate subscription cancellation"
            ));
        } else {
            subscription.cancelAtPeriodEnd();
            subscriptionRepository.save(subscription);
            billingCachePort.evict(subscription.tenantId());
        }
    }

    @Override
    @Transactional
    public TenantSubscription handleStartTrial(TenantId tenantId, PlanId planId, int trialDays) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(planId, "PlanId cannot be null");

        if (subscriptionRepository.existsActiveByTenantId(tenantId)) {
            throw new DuplicateActiveSubscriptionException(tenantId);
        }

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        String legalName = iamTenantValidationAclPort.getTenantLegalName(tenantId);
        String email = iamTenantValidationAclPort.getTenantContactEmail(tenantId);
        StripeCustomerId customerId = stripeGatewayPort.createCustomer(tenantId, legalName, email);

        TenantSubscription subscription = TenantSubscription.startTrial(tenantId, plan.id(), customerId, trialDays);
        TenantSubscription saved = subscriptionRepository.save(subscription);

        billingCachePort.cacheSubscriptionActive(tenantId, true);
        return saved;
    }

    @Override
    public String handleCreateCustomerPortalSession(TenantId tenantId, String returnUrl) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(returnUrl, "returnUrl cannot be null");

        TenantSubscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new SubscriptionNotFoundException("No subscription found for tenant: " + tenantId.value()));

        if (subscription.stripeCustomerId() == null || subscription.stripeCustomerId().value().isBlank()) {
            throw new SubscriptionNotFoundException("No Stripe customer associated with tenant: " + tenantId.value());
        }

        return stripeGatewayPort.createCustomerPortalSession(subscription.stripeCustomerId(), returnUrl);
    }
}
