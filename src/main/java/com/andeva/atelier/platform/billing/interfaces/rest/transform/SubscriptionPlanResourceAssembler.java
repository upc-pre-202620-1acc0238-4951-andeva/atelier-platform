package com.andeva.atelier.platform.billing.interfaces.rest.transform;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SubscriptionPlanResource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Assembler transforming {@link SubscriptionPlan} domain aggregates into {@link SubscriptionPlanResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SubscriptionPlanResourceAssembler {

    private final PlanFeatureResourceAssembler featureResourceAssembler;

    public SubscriptionPlanResourceAssembler(PlanFeatureResourceAssembler featureResourceAssembler) {
        this.featureResourceAssembler = Objects.requireNonNull(featureResourceAssembler, "PlanFeatureResourceAssembler cannot be null");
    }

    /**
     * Transforms a SubscriptionPlan aggregate root into its REST representation.
     *
     * @param plan domain plan aggregate root
     * @return REST resource
     */
    public SubscriptionPlanResource toResource(SubscriptionPlan plan) {
        Objects.requireNonNull(plan, "SubscriptionPlan cannot be null");

        TenantQuotaLimitsDto quotaLimitsDto = toQuotaDto(plan.quotaLimits());

        return new SubscriptionPlanResource(
                plan.id().value(),
                plan.stripePriceId().value(),
                plan.name(),
                plan.tier().name(),
                plan.pricing().price().amount(),
                plan.pricing().price().currency().name(),
                plan.pricing().billingCycle().name(),
                quotaLimitsDto,
                featureResourceAssembler.toResourceList(plan.features()),
                plan.isActive(),
                null, // createdAt (not maintained on domain aggregate)
                null  // updatedAt (not maintained on domain aggregate)
        );
    }

    /**
     * Transforms a collection of domain plans into a list of REST resources.
     *
     * @param plans collection of domain plans
     * @return unmodifiable list of resources
     */
    public List<SubscriptionPlanResource> toResourceList(List<SubscriptionPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return Collections.emptyList();
        }
        return plans.stream()
                .filter(Objects::nonNull)
                .map(this::toResource)
                .toList();
    }

    /**
     * Maps domain quota limits into DTO format.
     *
     * @param limits domain quotas
     * @return quota DTO
     */
    public static TenantQuotaLimitsDto toQuotaDto(TenantQuotaLimits limits) {
        if (limits == null) {
            return null;
        }
        return new TenantQuotaLimitsDto(
                limits.maxBranches(),
                limits.maxActiveStaff(),
                limits.maxActiveObd2Devices(),
                limits.maxPhotosPerWorkOrder(),
                limits.maxMonthlyAiReports(),
                limits.companyRegistrationAllowed(),
                limits.multiWarehouseAllowed(),
                limits.marketplaceListed(),
                limits.maxMonthlyWorkOrders(),
                limits.iotTelemetryEnabled(),
                limits.aiDiagnosticsEnabled()
        );
    }
}
