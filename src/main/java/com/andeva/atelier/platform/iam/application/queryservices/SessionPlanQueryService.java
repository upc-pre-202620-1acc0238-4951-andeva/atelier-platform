package com.andeva.atelier.platform.iam.application.queryservices;

import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort.SessionPlan;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Optional;

/**
 * Plan of the workshop that the session carries, so the staff app can hide the modules the plan does not include.
 *
 * @author Joel Huamani Estefanero
 */
public interface SessionPlanQueryService {

    Optional<SessionPlan> planOf(TenantId tenantId);
}
