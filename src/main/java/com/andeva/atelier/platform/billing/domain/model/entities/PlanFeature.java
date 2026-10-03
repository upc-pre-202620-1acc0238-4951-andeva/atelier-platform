package com.andeva.atelier.platform.billing.domain.model.entities;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanFeatureId;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;

import java.util.Objects;

/**
 * Child Entity representing an individual feature toggle or functional capability within a commercial subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
public class PlanFeature {

    private final PlanFeatureId id;
    private final PlanId planId;
    private final String featureKey;
    private String description;
    private boolean isEnabled;

    public PlanFeature(PlanFeatureId id, PlanId planId, String featureKey, String description, boolean isEnabled) {
        this.id = Objects.requireNonNull(id, "PlanFeatureId cannot be null");
        this.planId = Objects.requireNonNull(planId, "PlanId cannot be null");
        this.featureKey = Objects.requireNonNull(featureKey, "Feature key cannot be null").trim().toUpperCase();
        if (this.featureKey.isBlank()) {
            throw new IllegalArgumentException("Feature key cannot be blank");
        }
        this.description = description != null ? description.trim() : "";
        this.isEnabled = isEnabled;
    }

    public static PlanFeature of(PlanFeatureId id, PlanId planId, String featureKey, String description, boolean isEnabled) {
        return new PlanFeature(id, planId, featureKey, description, isEnabled);
    }

    public static PlanFeature create(PlanId planId, String featureKey, String description, boolean isEnabled) {
        return new PlanFeature(PlanFeatureId.generate(), planId, featureKey, description, isEnabled);
    }

    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription != null ? newDescription.trim() : "";
    }

    public PlanFeatureId id() {
        return id;
    }

    public PlanFeatureId getId() {
        return id;
    }

    public PlanId planId() {
        return planId;
    }

    public PlanId getPlanId() {
        return planId;
    }

    public String featureKey() {
        return featureKey;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public String description() {
        return description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanFeature that = (PlanFeature) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
