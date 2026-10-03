package com.andeva.atelier.platform.billing.application.internal.queryservices;

import com.andeva.atelier.platform.billing.application.queryservices.SubscriptionPlanQueryService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.queries.GetSubscriptionPlanByIdQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.ListActivePlansQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of SubscriptionPlanQueryService for retrieving commercial plan details
 * and catalog listings.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class SubscriptionPlanQueryServiceImpl implements SubscriptionPlanQueryService {

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanQueryServiceImpl(SubscriptionPlanRepository planRepository) {
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
    }

    @Override
    public Optional<SubscriptionPlan> handle(GetSubscriptionPlanByIdQuery query) {
        Objects.requireNonNull(query, "GetSubscriptionPlanByIdQuery cannot be null");
        return planRepository.findById(query.planId());
    }

    @Override
    public List<SubscriptionPlan> handle(ListActivePlansQuery query) {
        Objects.requireNonNull(query, "ListActivePlansQuery cannot be null");
        return planRepository.findAllActive();
    }

    @Override
    public Optional<SubscriptionPlan> findByStripePriceId(StripePriceId stripePriceId) {
        Objects.requireNonNull(stripePriceId, "StripePriceId cannot be null");
        return planRepository.findByStripePriceId(stripePriceId);
    }
}
