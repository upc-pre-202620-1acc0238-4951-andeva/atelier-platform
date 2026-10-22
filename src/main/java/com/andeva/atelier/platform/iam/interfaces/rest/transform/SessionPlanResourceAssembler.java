package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.application.queryservices.SessionPlanQueryService;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.SessionPlanResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Builds the plan section of a session response.
 */
public final class SessionPlanResourceAssembler {

    private SessionPlanResourceAssembler() {
    }

    /**
     * @return the tenant's plan, or null without a tenant or when the plan cannot be read
     */
    public static SessionPlanResource toResource(SessionPlanQueryService service, TenantId tenantId) {
        if (service == null || tenantId == null) {
            return null;
        }
        return service.planOf(tenantId)
                .map(plan -> new SessionPlanResource(plan.planName(), plan.tier(), plan.status(), plan.accessGranted(),
                        plan.iotTelemetryEnabled(), plan.aiDiagnosticsEnabled()))
                .orElse(null);
    }
}
