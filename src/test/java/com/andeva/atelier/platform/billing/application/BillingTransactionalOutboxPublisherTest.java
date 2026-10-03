package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.internal.eventhandlers.BillingTransactionalOutboxPublisher;
import com.andeva.atelier.platform.billing.interfaces.events.TenantPlanUpgradedIntegrationEvent;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionStatusChangedIntegrationEvent;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionSuspendedIntegrationEvent;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit test suite for {@link BillingTransactionalOutboxPublisher}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Billing Transactional Outbox Publisher Tests")
class BillingTransactionalOutboxPublisherTest {

    @Mock
    private OutboxMessageJpaRepository outboxRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    private BillingTransactionalOutboxPublisher outboxPublisher;

    @BeforeEach
    void setUp() {
        outboxPublisher = new BillingTransactionalOutboxPublisher(outboxRepository, objectMapper);
    }

    @Test
    @DisplayName("Should serialize and persist TenantSubscriptionStatusChangedIntegrationEvent")
    void shouldPersistStatusChangedEvent() {
        UUID subId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        TenantSubscriptionStatusChangedIntegrationEvent event = TenantSubscriptionStatusChangedIntegrationEvent.of(
                subId,
                tenantId,
                "TRIALING",
                "ACTIVE"
        );

        outboxPublisher.on(event);

        ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxMessagePersistenceEntity entity = captor.getValue();
        assertThat(entity.getAggregateType()).isEqualTo("TenantSubscription");
        assertThat(entity.getAggregateId()).isEqualTo(subId.toString());
        assertThat(entity.getEventType()).isEqualTo("TenantSubscriptionStatusChangedIntegrationEvent");
        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(entity.getPayload()).contains("ACTIVE");
    }

    @Test
    @DisplayName("Should serialize and persist TenantPlanUpgradedIntegrationEvent")
    void shouldPersistPlanUpgradedEvent() {
        UUID subId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID oldPlanId = UUID.randomUUID();
        UUID newPlanId = UUID.randomUUID();

        TenantPlanUpgradedIntegrationEvent event = TenantPlanUpgradedIntegrationEvent.of(
                subId,
                tenantId,
                oldPlanId,
                newPlanId,
                "Enterprise Plan",
                "ENTERPRISE"
        );

        outboxPublisher.on(event);

        ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxMessagePersistenceEntity entity = captor.getValue();
        assertThat(entity.getAggregateType()).isEqualTo("TenantSubscription");
        assertThat(entity.getEventType()).isEqualTo("TenantPlanUpgradedIntegrationEvent");
        assertThat(entity.getPayload()).contains("Enterprise Plan");
    }

    @Test
    @DisplayName("Should serialize and persist TenantSubscriptionSuspendedIntegrationEvent")
    void shouldPersistSuspendedEvent() {
        UUID subId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        TenantSubscriptionSuspendedIntegrationEvent event = TenantSubscriptionSuspendedIntegrationEvent.of(
                subId,
                tenantId,
                "Past due billing violation"
        );

        outboxPublisher.on(event);

        ArgumentCaptor<OutboxMessagePersistenceEntity> captor = ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxMessagePersistenceEntity entity = captor.getValue();
        assertThat(entity.getAggregateType()).isEqualTo("TenantSubscription");
        assertThat(entity.getEventType()).isEqualTo("TenantSubscriptionSuspendedIntegrationEvent");
        assertThat(entity.getPayload()).contains("Past due billing violation");
    }
}
