package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.internal.eventhandlers.TenantLifecycleIntegrationEventHandler;
import com.andeva.atelier.platform.billing.application.internal.eventhandlers.TrialSubscriptionReconciler;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.IamTenantValidationAclPort;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Workshops left without a subscription get their free trial at start-up")
class TrialSubscriptionReconcilerTest {

    @Test
    @DisplayName("Only workshops without a subscription are given a trial")
    void shouldGrantTrialOnlyWhereMissing() {
        TenantId withSubscription = TenantId.of(UUID.randomUUID());
        TenantId withoutSubscription = TenantId.of(UUID.randomUUID());
        IamTenantValidationAclPort iam = mock(IamTenantValidationAclPort.class);
        TenantSubscriptionRepository subscriptions = mock(TenantSubscriptionRepository.class);
        TenantLifecycleIntegrationEventHandler provisioner = mock(TenantLifecycleIntegrationEventHandler.class);
        when(iam.listTenantIds()).thenReturn(List.of(withSubscription, withoutSubscription));
        when(subscriptions.findByTenantId(withSubscription)).thenReturn(Optional.of(mock(TenantSubscription.class)));
        when(subscriptions.findByTenantId(withoutSubscription)).thenReturn(Optional.empty());
        when(provisioner.provisionTrial(withoutSubscription, null)).thenReturn(true);

        int granted = new TrialSubscriptionReconciler(iam, subscriptions, provisioner).reconcile();

        assertThat(granted).isEqualTo(1);
        verify(provisioner).provisionTrial(withoutSubscription, null);
        verify(provisioner, never()).provisionTrial(withSubscription, null);
    }

    @Test
    @DisplayName("Without a plan catalog nothing is granted and nothing fails")
    void shouldGrantNothingWithoutCatalog() {
        TenantId tenant = TenantId.of(UUID.randomUUID());
        IamTenantValidationAclPort iam = mock(IamTenantValidationAclPort.class);
        TenantSubscriptionRepository subscriptions = mock(TenantSubscriptionRepository.class);
        TenantLifecycleIntegrationEventHandler provisioner = mock(TenantLifecycleIntegrationEventHandler.class);
        when(iam.listTenantIds()).thenReturn(List.of(tenant));
        when(subscriptions.findByTenantId(any())).thenReturn(Optional.empty());
        when(provisioner.provisionTrial(tenant, null)).thenReturn(false);

        assertThat(new TrialSubscriptionReconciler(iam, subscriptions, provisioner).reconcile()).isZero();
    }
}
