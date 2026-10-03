package com.andeva.atelier.platform.billing.interfaces.rest.transform;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.TenantSubscriptionResource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Assembler transforming {@link TenantSubscription} domain aggregates into {@link TenantSubscriptionResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TenantSubscriptionResourceAssembler {

    /**
     * Transforms a TenantSubscription aggregate root and its commercial plan into its REST representation.
     *
     * @param subscription non-null tenant subscription aggregate
     * @param plan         associated subscription plan
     * @return REST resource
     */
    public TenantSubscriptionResource toResource(TenantSubscription subscription, SubscriptionPlan plan) {
        Objects.requireNonNull(subscription, "TenantSubscription cannot be null");
        String planName = plan != null ? plan.name() : "Plan " + subscription.planId().value();
        String tier = plan != null ? plan.tier().name() : "UNKNOWN";
        TenantQuotaLimitsDto quotaLimitsDto = plan != null ? SubscriptionPlanResourceAssembler.toQuotaDto(plan.quotaLimits()) : null;

        return toResourceInternal(subscription, planName, tier, quotaLimitsDto);
    }

    /**
     * Transforms a TenantSubscription aggregate root into its REST representation using resolved metadata.
     *
     * @param subscription non-null tenant subscription aggregate
     * @param planName     resolved commercial plan denomination
     * @param quotas       resolved operational quota limits
     * @return REST resource
     */
    public TenantSubscriptionResource toResource(TenantSubscription subscription, String planName, TenantQuotaLimits quotas) {
        Objects.requireNonNull(subscription, "TenantSubscription cannot be null");
        TenantQuotaLimitsDto quotaLimitsDto = SubscriptionPlanResourceAssembler.toQuotaDto(quotas);
        return toResourceInternal(subscription, planName != null ? planName : "Plan", "PRO", quotaLimitsDto);
    }

    private TenantSubscriptionResource toResourceInternal(
            TenantSubscription subscription,
            String planName,
            String tier,
            TenantQuotaLimitsDto quotaLimitsDto
    ) {
        return new TenantSubscriptionResource(
                subscription.id().value(),
                subscription.tenantId().value(),
                subscription.planId().value(),
                planName,
                tier,
                subscription.status().name(),
                subscription.currentPeriod() != null ? subscription.currentPeriod().startDate() : null,
                subscription.currentPeriod() != null ? subscription.currentPeriod().endDate() : null,
                subscription.isCancelAtPeriodEnd(),
                subscription.trialEndDate().orElse(null),
                quotaLimitsDto,
                subscription.isAccessGranted()
        );
    }
}
