package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort.SessionPlan;
import com.andeva.atelier.platform.iam.application.queryservices.SessionPlanQueryService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class SessionPlanQueryServiceImpl implements SessionPlanQueryService {

    private static final Logger log = LoggerFactory.getLogger(SessionPlanQueryServiceImpl.class);

    private final SubscriptionQuotaPort subscriptionQuotaPort;

    public SessionPlanQueryServiceImpl(SubscriptionQuotaPort subscriptionQuotaPort) {
        this.subscriptionQuotaPort = Objects.requireNonNull(subscriptionQuotaPort, "subscriptionQuotaPort cannot be null");
    }

    @Override
    public Optional<SessionPlan> planOf(TenantId tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }
        try {
            return subscriptionQuotaPort.fetchSessionPlan(tenantId);
        } catch (RuntimeException e) {
            // The plan is a hint for the UI: a billing failure must never block a sign-in
            log.warn("Plan of tenant {} could not be read for the session: {}", tenantId.value(), e.getMessage());
            return Optional.empty();
        }
    }
}
