package com.andeva.atelier.platform.billing.domain.model.aggregates;

import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.events.SubscriptionPlanCreatedEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root representing a commercial SaaS subscription plan offered to automotive workshops.
 *
 * @author Joel Huamani Estefanero
 */
public class SubscriptionPlan extends AbstractDomainAggregateRoot<SubscriptionPlan> {

    private final PlanId id;
    private final StripePriceId stripePriceId;
    private String name;
    private PlanTier tier;
    private PlanPricing pricing;
    private TenantQuotaLimits quotaLimits;
    private final List<PlanFeature> features;
    private boolean isActive;

    public SubscriptionPlan(
            PlanId id,
            StripePriceId stripePriceId,
            String name,
            PlanTier tier,
            PlanPricing pricing,
            TenantQuotaLimits quotaLimits,
            List<PlanFeature> features,
            boolean isActive
    ) {
        this.id = Objects.requireNonNull(id, "PlanId cannot be null");
        this.stripePriceId = Objects.requireNonNull(stripePriceId, "StripePriceId cannot be null");
        this.name = Objects.requireNonNull(name, "Plan name cannot be null").trim();
        if (this.name.isBlank()) {
            throw new IllegalArgumentException("Plan name cannot be blank");
        }
        this.tier = Objects.requireNonNull(tier, "PlanTier cannot be null");
        this.pricing = Objects.requireNonNull(pricing, "PlanPricing cannot be null");
        this.quotaLimits = Objects.requireNonNull(quotaLimits, "TenantQuotaLimits cannot be null");
        if (quotaLimits.maxBranches() != -1 && quotaLimits.maxBranches() < 1) {
            throw new IllegalArgumentException("A subscription plan must authorize at least 1 branch (or -1 for unlimited)");
        }
        if (quotaLimits.maxActiveStaff() != -1 && quotaLimits.maxActiveStaff() < 1) {
            throw new IllegalArgumentException("A subscription plan must authorize at least 1 active staff member (or -1 for unlimited)");
        }
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
        this.isActive = isActive;
    }

    public SubscriptionPlan(
            PlanId id,
            StripePriceId stripePriceId,
            String name,
            PlanTier tier,
            PlanPricing pricing,
            TenantQuotaLimits quotaLimits,
            boolean isActive
    ) {
        this(id, stripePriceId, name, tier, pricing, quotaLimits, List.of(), isActive);
    }

    public static SubscriptionPlan create(
            StripePriceId stripePriceId,
            String name,
            PlanTier tier,
            PlanPricing pricing,
            TenantQuotaLimits quotaLimits
    ) {
        return create(stripePriceId, name, tier, pricing, quotaLimits, Collections.emptyList());
    }

    public static SubscriptionPlan create(
            StripePriceId stripePriceId,
            String name,
            PlanTier tier,
            PlanPricing pricing,
            TenantQuotaLimits quotaLimits,
            List<PlanFeature> features
    ) {
        PlanId planId = PlanId.generate();
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                stripePriceId,
                name,
                tier,
                pricing,
                quotaLimits,
                features,
                true
        );
        plan.registerDomainEvent(SubscriptionPlanCreatedEvent.of(planId, name, tier, pricing.price()));
        return plan;
    }

    public void updateDetails(String newName, PlanPricing newPricing, TenantQuotaLimits newQuotaLimits) {
        Objects.requireNonNull(newName, "New plan name cannot be null");
        if (newName.isBlank()) {
            throw new IllegalArgumentException("New plan name cannot be blank");
        }
        this.name = newName.trim();
        this.pricing = Objects.requireNonNull(newPricing, "New PlanPricing cannot be null");
        this.quotaLimits = Objects.requireNonNull(newQuotaLimits, "New TenantQuotaLimits cannot be null");
        if (newQuotaLimits.maxBranches() != -1 && newQuotaLimits.maxBranches() < 1) {
            throw new IllegalArgumentException("A subscription plan must authorize at least 1 branch (or -1 for unlimited)");
        }
        if (newQuotaLimits.maxActiveStaff() != -1 && newQuotaLimits.maxActiveStaff() < 1) {
            throw new IllegalArgumentException("A subscription plan must authorize at least 1 active staff member (or -1 for unlimited)");
        }
    }

    public void addFeature(PlanFeature feature) {
        Objects.requireNonNull(feature, "PlanFeature cannot be null");
        removeFeature(feature.featureKey());
        this.features.add(feature);
    }

    public void removeFeature(String featureKey) {
        if (featureKey == null) return;
        this.features.removeIf(f -> f.featureKey().equalsIgnoreCase(featureKey));
    }

    public boolean hasFeature(String featureKey) {
        if (featureKey == null) return false;
        return this.features.stream()
                .anyMatch(f -> f.featureKey().equalsIgnoreCase(featureKey) && f.isEnabled());
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public PlanId id() {
        return id;
    }

    public PlanId getId() {
        return id;
    }

    public StripePriceId stripePriceId() {
        return stripePriceId;
    }

    public StripePriceId getStripePriceId() {
        return stripePriceId;
    }

    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }

    public PlanTier tier() {
        return tier;
    }

    public PlanTier getTier() {
        return tier;
    }

    public PlanPricing pricing() {
        return pricing;
    }

    public PlanPricing getPricing() {
        return pricing;
    }

    public TenantQuotaLimits quotaLimits() {
        return quotaLimits;
    }

    public TenantQuotaLimits getQuotaLimits() {
        return quotaLimits;
    }

    public List<PlanFeature> features() {
        return Collections.unmodifiableList(features);
    }

    public List<PlanFeature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    public boolean isActive() {
        return isActive;
    }
}
