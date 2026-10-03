package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanFeatureId;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.PlanFeaturePersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembler transforming between {@link SubscriptionPlan} Domain Aggregate Root
 * and {@link SubscriptionPlanPersistenceEntity} JPA entity.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SubscriptionPlanPersistenceAssembler {

    /**
     * Converts a domain aggregate into a JPA persistence entity.
     *
     * @param domain domain aggregate root
     * @return JPA persistence entity
     */
    public SubscriptionPlanPersistenceEntity toEntity(SubscriptionPlan domain) {
        if (domain == null) {
            return null;
        }

        SubscriptionPlanPersistenceEntity entity = new SubscriptionPlanPersistenceEntity(domain.id().value());
        entity.setStripePriceId(domain.stripePriceId().value());
        entity.setName(domain.name());
        entity.setTier(domain.tier());
        entity.setPrice(domain.pricing().price().amount());
        entity.setCurrency(domain.pricing().price().currency().name());
        entity.setBillingCycle(domain.pricing().billingCycle());

        TenantQuotaLimits quotas = domain.quotaLimits();
        entity.setMaxBranches(quotas.maxBranches());
        entity.setMaxActiveStaff(quotas.maxActiveStaff());
        entity.setMaxActiveObd2Devices(quotas.maxActiveObd2Devices());
        entity.setMaxPhotosPerWorkOrder(quotas.maxPhotosPerWorkOrder());
        entity.setMaxMonthlyAiReports(quotas.maxMonthlyAiReports());
        entity.setCompanyRegistrationAllowed(quotas.companyRegistrationAllowed());
        entity.setMultiWarehouseAllowed(quotas.multiWarehouseAllowed());
        entity.setMarketplaceListed(quotas.marketplaceListed());
        entity.setMaxMonthlyWorkOrders(quotas.maxMonthlyWorkOrders());
        entity.setIotTelemetryEnabled(quotas.iotTelemetryEnabled());
        entity.setAiDiagnosticsEnabled(quotas.aiDiagnosticsEnabled());
        entity.setActive(domain.isActive());

        if (domain.features() != null) {
            for (PlanFeature feature : domain.features()) {
                PlanFeaturePersistenceEntity fEntity = new PlanFeaturePersistenceEntity(
                        feature.id() != null ? feature.id().value() : null,
                        entity,
                        feature.featureKey(),
                        feature.description(),
                        feature.isEnabled()
                );
                entity.addFeature(fEntity);
            }
        }

        return entity;
    }

    /**
     * Reconstitutes a domain aggregate from a JPA persistence entity.
     *
     * @param entity JPA persistence entity
     * @return domain aggregate root
     */
    public SubscriptionPlan toDomain(SubscriptionPlanPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        PlanId planId = PlanId.of(entity.getId());
        StripePriceId priceId = new StripePriceId(entity.getStripePriceId());
        Currency currency = Currency.valueOf(entity.getCurrency() != null ? entity.getCurrency() : "USD");
        Money money = Money.of(entity.getPrice(), currency);
        PlanPricing pricing = PlanPricing.of(money, entity.getBillingCycle());

        TenantQuotaLimits quotaLimits = new TenantQuotaLimits(
                entity.getMaxBranches(),
                entity.getMaxActiveStaff(),
                entity.getMaxActiveObd2Devices(),
                entity.getMaxPhotosPerWorkOrder(),
                entity.getMaxMonthlyAiReports(),
                entity.isCompanyRegistrationAllowed(),
                entity.isMultiWarehouseAllowed(),
                entity.isMarketplaceListed(),
                entity.getMaxMonthlyWorkOrders(),
                entity.isIotTelemetryEnabled(),
                entity.isAiDiagnosticsEnabled()
        );

        List<PlanFeature> features = new ArrayList<>();
        if (entity.getFeatures() != null) {
            for (PlanFeaturePersistenceEntity fEntity : entity.getFeatures()) {
                PlanFeature feature = new PlanFeature(
                        fEntity.getId() != null ? PlanFeatureId.of(fEntity.getId()) : PlanFeatureId.generate(),
                        planId,
                        fEntity.getFeatureKey(),
                        fEntity.getDescription(),
                        fEntity.isEnabled()
                );
                features.add(feature);
            }
        }

        return new SubscriptionPlan(
                planId,
                priceId,
                entity.getName(),
                entity.getTier(),
                pricing,
                quotaLimits,
                features,
                entity.isActive()
        );
    }
}
