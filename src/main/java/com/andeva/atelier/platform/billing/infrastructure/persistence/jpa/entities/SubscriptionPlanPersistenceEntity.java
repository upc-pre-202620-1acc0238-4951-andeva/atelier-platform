package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.BillingCycleConverter;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.PlanTierConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code plans} relational table.
 * Persists the commercial subscription plans catalog, fee structures, and operational quota limits.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "plans",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_plans_stripe_price", columnNames = {"stripe_price_id"})
        },
        indexes = {
                @Index(name = "idx_plans_tier", columnList = "tier"),
                @Index(name = "idx_plans_tier_active", columnList = "tier, is_active")
        }
)
public class SubscriptionPlanPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "stripe_price_id", nullable = false, length = 100, unique = true)
    private String stripePriceId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Convert(converter = PlanTierConverter.class)
    @Column(name = "tier", nullable = false, length = 20)
    private PlanTier tier;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Convert(converter = BillingCycleConverter.class)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    @Column(name = "max_branches", nullable = false)
    private int maxBranches;

    @Column(name = "max_active_staff", nullable = false)
    private int maxActiveStaff;

    @Column(name = "max_active_obd2_devices", nullable = false)
    private int maxActiveObd2Devices;

    @Column(name = "max_photos_per_work_order", nullable = false)
    private int maxPhotosPerWorkOrder = 10;

    @Column(name = "max_monthly_ai_reports", nullable = false)
    private int maxMonthlyAiReports;

    @Column(name = "company_registration_allowed", nullable = false)
    private boolean companyRegistrationAllowed;

    @Column(name = "multi_warehouse_allowed", nullable = false)
    private boolean multiWarehouseAllowed;

    @Column(name = "marketplace_listed", nullable = false)
    private boolean marketplaceListed;

    @Column(name = "max_monthly_work_orders", nullable = false)
    private int maxMonthlyWorkOrders;

    @Column(name = "iot_telemetry_enabled", nullable = false)
    private boolean iotTelemetryEnabled;

    @Column(name = "ai_diagnostics_enabled", nullable = false)
    private boolean aiDiagnosticsEnabled;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PlanFeaturePersistenceEntity> features = new ArrayList<>();

    public SubscriptionPlanPersistenceEntity(UUID id) {
        super(id);
    }

    public void addFeature(PlanFeaturePersistenceEntity feature) {
        if (feature != null) {
            features.add(feature);
            feature.setPlan(this);
        }
    }

    public void removeFeature(PlanFeaturePersistenceEntity feature) {
        if (feature != null) {
            features.remove(feature);
            feature.setPlan(null);
        }
    }
}
