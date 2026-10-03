package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.TenantSubscriptionPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

/**
 * Assembler transforming between {@link TenantSubscription} Domain Aggregate Root
 * and {@link TenantSubscriptionPersistenceEntity} JPA entity.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TenantSubscriptionPersistenceAssembler {

    /**
     * Converts a domain aggregate into a JPA persistence entity.
     *
     * @param domain domain aggregate root
     * @return JPA persistence entity
     */
    public TenantSubscriptionPersistenceEntity toEntity(TenantSubscription domain) {
        if (domain == null) {
            return null;
        }

        TenantSubscriptionPersistenceEntity entity = new TenantSubscriptionPersistenceEntity(domain.id().value());
        entity.setTenantId(domain.tenantId().value());
        entity.setPlanId(domain.planId().value());
        entity.setStripeCustomerId(domain.stripeCustomerId().value());
        entity.setStripeSubscriptionId(domain.stripeSubscriptionId() != null ? domain.stripeSubscriptionId().value() : "");
        entity.setStatus(domain.status());
        entity.setCurrentPeriodStart(domain.currentPeriod().startDate());
        entity.setCurrentPeriodEnd(domain.currentPeriod().endDate());
        entity.setCancelAtPeriodEnd(domain.isCancelAtPeriodEnd());
        entity.setCanceledAt(domain.canceledAt().orElse(null));
        entity.setTrialEndDate(domain.trialEndDate().orElse(null));

        return entity;
    }

    /**
     * Reconstitutes a domain aggregate from a JPA persistence entity.
     *
     * @param entity JPA persistence entity
     * @return domain aggregate root
     */
    public TenantSubscription toDomain(TenantSubscriptionPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        SubscriptionId id = SubscriptionId.of(entity.getId());
        TenantId tenantId = new TenantId(entity.getTenantId());
        PlanId planId = PlanId.of(entity.getPlanId());
        StripeCustomerId customerId = new StripeCustomerId(entity.getStripeCustomerId());
        StripeSubscriptionId subId = entity.getStripeSubscriptionId() != null && !entity.getStripeSubscriptionId().isBlank()
                ? new StripeSubscriptionId(entity.getStripeSubscriptionId())
                : null;
        SubscriptionPeriod period = SubscriptionPeriod.of(entity.getCurrentPeriodStart(), entity.getCurrentPeriodEnd());

        return new TenantSubscription(
                id,
                tenantId,
                planId,
                customerId,
                subId,
                entity.getStatus(),
                period,
                entity.isCancelAtPeriodEnd(),
                entity.getCanceledAt(),
                entity.getTrialEndDate()
        );
    }
}
