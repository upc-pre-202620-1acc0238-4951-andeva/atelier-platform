package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code plan_features} relational table.
 * Persists granular capability flags and modular feature definitions packaged into commercial plans.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "plan_features",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_plan_features_plan_key", columnNames = {"plan_id", "feature_key"})
        },
        indexes = {
                @Index(name = "idx_plan_features_plan", columnList = "plan_id"),
                @Index(name = "idx_plan_features_lookup", columnList = "plan_id, is_enabled")
        }
)
public class PlanFeaturePersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlanPersistenceEntity plan;

    @Column(name = "feature_key", nullable = false, length = 50)
    private String featureKey;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public PlanFeaturePersistenceEntity(
            UUID id,
            SubscriptionPlanPersistenceEntity plan,
            String featureKey,
            String description,
            boolean isEnabled
    ) {
        this.id = id;
        this.plan = plan;
        this.featureKey = featureKey;
        this.description = description;
        this.isEnabled = isEnabled;
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanFeaturePersistenceEntity that = (PlanFeaturePersistenceEntity) o;
        if (id != null && that.id != null) {
            return java.util.Objects.equals(id, that.id);
        }
        return java.util.Objects.equals(featureKey, that.featureKey) &&
                java.util.Objects.equals(plan != null ? plan.getId() : null, that.plan != null ? that.plan.getId() : null);
    }

    @Override
    public int hashCode() {
        return id != null ? java.util.Objects.hash(id) : java.util.Objects.hash(plan != null ? plan.getId() : null, featureKey);
    }
}
