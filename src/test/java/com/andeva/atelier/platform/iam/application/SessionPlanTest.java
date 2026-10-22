package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantSubscriptionStatusDto;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort;
import com.andeva.atelier.platform.iam.application.internal.queryservices.SessionPlanQueryServiceImpl;
import com.andeva.atelier.platform.iam.infrastructure.external.quota.SubscriptionQuotaAdapter;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.SessionPlanResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.SessionPlanResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("The session carries the workshop plan so the app hides modules the plan lacks")
class SessionPlanTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());

    @Test
    @DisplayName("A PRO trial without AI exposes telemetry on and AI diagnostics off")
    void shouldExposePlanFlags() {
        SubscriptionContextFacade facade = mock(SubscriptionContextFacade.class);
        when(facade.getTenantSubscriptionStatus(tenantId.value())).thenReturn(new TenantSubscriptionStatusDto(
                tenantId.value(), "Pro", "PRO", "TRIALING", true, Instant.now().plusSeconds(86_400), false));
        when(facade.getTenantQuotaLimits(tenantId.value())).thenReturn(
                new TenantQuotaLimitsDto(2, 10, 5, 20, 0, true, false, false, 200, true, false));
        var service = new SessionPlanQueryServiceImpl(new SubscriptionQuotaAdapter(facade));

        SessionPlanResource plan = SessionPlanResourceAssembler.toResource(service, tenantId);

        assertThat(plan).isNotNull();
        assertThat(plan.tier()).isEqualTo("PRO");
        assertThat(plan.status()).isEqualTo("TRIALING");
        assertThat(plan.accessGranted()).isTrue();
        assertThat(plan.iotTelemetryEnabled()).isTrue();
        assertThat(plan.aiDiagnosticsEnabled()).isFalse();
    }

    @Test
    @DisplayName("A billing failure never blocks the sign-in: the session goes without the plan")
    void shouldOmitPlanWhenBillingFails() {
        SubscriptionQuotaPort failing = mock(SubscriptionQuotaPort.class);
        when(failing.fetchSessionPlan(tenantId)).thenThrow(new IllegalStateException("billing down"));

        assertThat(SessionPlanResourceAssembler.toResource(new SessionPlanQueryServiceImpl(failing), tenantId)).isNull();
        assertThat(SessionPlanResourceAssembler.toResource(new SessionPlanQueryServiceImpl(failing), null)).isNull();
    }
}
