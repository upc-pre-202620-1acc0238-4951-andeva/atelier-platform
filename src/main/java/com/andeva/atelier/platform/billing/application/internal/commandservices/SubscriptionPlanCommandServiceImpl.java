package com.andeva.atelier.platform.billing.application.internal.commandservices;

import com.andeva.atelier.platform.billing.application.commandservices.SubscriptionPlanCommandService;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidPlanPricingException;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.commands.CreateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.UpdateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Implementation of SubscriptionPlanCommandService managing the lifecycle and commercial parameters of subscription plans.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class SubscriptionPlanCommandServiceImpl implements SubscriptionPlanCommandService {

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanCommandServiceImpl(SubscriptionPlanRepository planRepository) {
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
    }

    @Override
    public PlanId handle(CreateSubscriptionPlanCommand command) {
        Objects.requireNonNull(command, "CreateSubscriptionPlanCommand cannot be null");

        planRepository.findByStripePriceId(command.stripePriceId()).ifPresent(existing -> {
            throw new InvalidPlanPricingException("A subscription plan already exists for Stripe price identifier: " + command.stripePriceId().value());
        });

        PlanPricing pricing = PlanPricing.of(command.price(), command.cycle());
        SubscriptionPlan plan = SubscriptionPlan.create(
                command.stripePriceId(),
                command.name(),
                command.tier(),
                pricing,
                command.quotas()
        );

        SubscriptionPlan saved = planRepository.save(plan);
        return saved.id();
    }

    @Override
    public void handle(UpdateSubscriptionPlanCommand command) {
        Objects.requireNonNull(command, "UpdateSubscriptionPlanCommand cannot be null");

        SubscriptionPlan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new PlanNotFoundException(command.planId()));

        PlanPricing pricing = PlanPricing.of(command.price(), command.cycle());
        plan.updateDetails(command.name(), pricing, command.quotas());
        planRepository.save(plan);
    }

    @Override
    public void handleActivate(PlanId planId) {
        Objects.requireNonNull(planId, "PlanId cannot be null");

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        plan.activate();
        planRepository.save(plan);
    }

    @Override
    public void handleDeactivate(PlanId planId) {
        Objects.requireNonNull(planId, "PlanId cannot be null");

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        plan.deactivate();
        planRepository.save(plan);
    }
}
